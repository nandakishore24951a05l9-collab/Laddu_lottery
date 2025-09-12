package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.model.CompletedWork;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.CompletedWorkRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/bookings")
public class EnderBookingController {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final CompletedWorkRepository completedWorkRepo;

    public EnderBookingController(BookingRepository bookingRepo, UserRepository userRepo, CompletedWorkRepository completedWorkRepo) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
        this.completedWorkRepo = completedWorkRepo;
    }

    // ===== SSE support =====
    private static final long SSE_TIMEOUT_MS = Duration.ofHours(6).toMillis();
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> streams = new ConcurrentHashMap<>();
    private final Map<Long, LiveSnapshot> live = new ConcurrentHashMap<>();

    private static final class LiveSnapshot {
        volatile String status;
        volatile Double techLat;
        volatile Double techLng;
        volatile Double custLat;
        volatile Double custLng;
        volatile Long   tsTech;
        volatile Long   tsCust;
    }

    private CopyOnWriteArrayList<SseEmitter> bucket(Long bookingId) {
        return streams.computeIfAbsent(bookingId, k -> new CopyOnWriteArrayList<>());
    }

    private LiveSnapshot snap(Long bookingId) {
        return live.computeIfAbsent(bookingId, k -> new LiveSnapshot());
    }

    private void sendEvent(Long bookingId, String name, Object data) {
        CopyOnWriteArrayList<SseEmitter> list = bucket(bookingId);
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter em : list) {
            try {
                em.send(SseEmitter.event().name(name).data(data, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                dead.add(em);
            }
        }
        if (!dead.isEmpty()) list.removeAll(dead);
    }

    // ===== Auth helpers =====
    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasTechAuthority() {
        Authentication a = auth();
        if (a == null) return false;
        return a.getAuthorities().stream().anyMatch(ga ->
                "ROLE_TECHNICIAN".equalsIgnoreCase(ga.getAuthority()) ||
                "technician".equalsIgnoreCase(ga.getAuthority())
        );
    }

    private boolean hasCustomerAuthority() {
        Authentication a = auth();
        if (a == null) return false;
        return a.getAuthorities().stream().anyMatch(ga ->
                "ROLE_CUSTOMER".equalsIgnoreCase(ga.getAuthority()) ||
                "customer".equalsIgnoreCase(ga.getAuthority())
        );
    }

    private String currentEmailOrNull() {
        Authentication a = auth();
        if (a == null || !a.isAuthenticated()) return null;

        Object p = a.getPrincipal();
        if (p instanceof UserDetails ud) return ud.getUsername();
        if (p instanceof String s) {
            if ("anonymousUser".equalsIgnoreCase(s)) return null;
            return s;
        }
        try {
            Method m = p.getClass().getMethod("getEmail");
            Object v = m.invoke(p);
            if (v != null) return v.toString();
        } catch (Exception ignored) {}
        try {
            Method m = p.getClass().getMethod("getUsername");
            Object v = m.invoke(p);
            if (v != null) return v.toString();
        } catch (Exception ignored) {}
        return null;
    }

    private User currentUserFromUsersTable() {
        String email = currentEmailOrNull();
        if (email == null) return null;
        return userRepo.findByEmail(email);
    }

    private static String shadowPhoneForEmail(String email) {
        long code = Math.abs((long) email.toLowerCase(Locale.ROOT).hashCode());
        long tail = code % 1_000_000_000L;
        return String.format("9%09d", tail);
    }

    private User requireTechUserEntity() {
        String email = currentEmailOrNull();
        if (email == null) return null;
        User u = userRepo.findByEmail(email);
        if (u != null) return u;
        if (!hasTechAuthority()) return null;
        User nu = new User();
        nu.setEmail(email);
        nu.setRole("technician");
        nu.setFirstName("Tech");
        nu.setLastName("Account");
        nu.setLocation("—");
        nu.setPhone(shadowPhoneForEmail(email));
        nu.setPassword("shadow-" + UUID.randomUUID());
        return userRepo.save(nu);
    }

    // ===== REST Endpoints =====

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        if (!hasCustomerAuthority()) {
            return ResponseEntity.status(403).body("Login as customer to create a booking.");
        }
        User customer = currentUserFromUsersTable();
        if (customer == null) return ResponseEntity.status(401).body("Not logged in.");

        Booking b = new Booking();
        b.setCustomer(customer);
        b.setServiceType(Objects.toString(body.getOrDefault("serviceType", "Carpenter")));

        Double lat = parseD(body.get("customerLat"));
        if (lat == null) lat = parseD(body.get("lat"));
        Double lng = parseD(body.get("customerLng"));
        if (lng == null) lng = parseD(body.get("lng"));

        if ((lat == null || lng == null) && body.containsKey("customerLocation")) {
            String[] parts = Objects.toString(body.get("customerLocation"), "").split(",");
            if (parts.length == 2) {
                try {
                    lat = Double.parseDouble(parts[0].trim());
                    lng = Double.parseDouble(parts[1].trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        if (lat == null || lng == null) {
            return ResponseEntity.badRequest().body("Missing customer location.");
        }

        b.setCustomerLat(lat);
        b.setCustomerLng(lng);
        b.setDescription(Objects.toString(body.getOrDefault("note", "")));
        b.setStatus("PENDING");

        bookingRepo.save(b);

        LiveSnapshot s = snap(b.getId());
        s.status = b.getStatus();
        s.custLat = b.getCustomerLat();
        s.custLng = b.getCustomerLng();
        s.tsCust  = System.currentTimeMillis();

        return ResponseEntity.ok(b);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        User me = currentUserFromUsersTable();
        if (me == null && !hasTechAuthority()) return ResponseEntity.status(401).build();

        Booking b = bookingRepo.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();

        boolean visible =
                hasTechAuthority() ||
                (me != null && b.getCustomer() != null && Objects.equals(b.getCustomer().getId(), me.getId())) ||
                (me != null && b.getTechnician() != null && Objects.equals(b.getTechnician().getId(), me.getId()));
        if (!visible) return ResponseEntity.status(403).body("Not allowed.");

        return ResponseEntity.ok(b);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending() {
        if (!hasTechAuthority()) return ResponseEntity.status(403).body("Only technicians can view pending.");
        try {
            return ResponseEntity.ok(bookingRepo.findByStatusIgnoreCaseOrderByCreatedAtDesc("PENDING"));
        } catch (Throwable t) {
            return ResponseEntity.ok(bookingRepo.findByStatus("PENDING"));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> my() {
        if (hasCustomerAuthority()) {
            User me = currentUserFromUsersTable();
            if (me == null) return ResponseEntity.status(401).build();
            return ResponseEntity.ok(bookingRepo.findByCustomer(me));
        } else if (hasTechAuthority()) {
            User tech = currentUserFromUsersTable();
            if (tech == null) {
                tech = requireTechUserEntity();
                if (tech == null) return ResponseEntity.status(401).build();
            }
            return ResponseEntity.ok(bookingRepo.findByTechnicianAndStatus(tech, "ACCEPTED"));
        }
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id) {
        if (!hasTechAuthority()) return ResponseEntity.status(403).body("Only technicians can accept.");
        User tech = currentUserFromUsersTable();
        if (tech == null) tech = requireTechUserEntity();
        if (tech == null) return ResponseEntity.status(401).build();

        Booking b = bookingRepo.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();
        if (!"PENDING".equalsIgnoreCase(b.getStatus())) {
            return ResponseEntity.badRequest().body("Booking already handled.");
        }
        if (b.getCustomerLat() == null || b.getCustomerLng() == null) {
            return ResponseEntity.badRequest().body("This request has no customer location");
        }

        b.setTechnician(tech);
        b.setStatus("ACCEPTED");
        bookingRepo.save(b);

        LiveSnapshot s = snap(id);
        s.status = "ACCEPTED";
        sendEvent(id, "status", Map.of("status", "ACCEPTED", "bookingId", id));

        return ResponseEntity.ok(b);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        if (!hasTechAuthority()) return ResponseEntity.status(403).body("Only technicians can reject.");
        Booking b = bookingRepo.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();
        if (!"PENDING".equalsIgnoreCase(b.getStatus())) {
            return ResponseEntity.badRequest().body("Booking already handled.");
        }

        b.setStatus("REJECTED");
        bookingRepo.save(b);

        LiveSnapshot s = snap(id);
        s.status = "REJECTED";
        sendEvent(id, "status", Map.of("status", "REJECTED", "bookingId", id));

        return ResponseEntity.ok(b);
    }

    /** ✅ FIXED complete() */
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> payload) {
        String email = currentEmailOrNull();
        if (email == null) return ResponseEntity.status(401).build();

        User me = userRepo.findByEmail(email);
        if (me == null && hasTechAuthority()) me = requireTechUserEntity();

        Booking b = bookingRepo.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();

        boolean allowed =
                (me != null && b.getCustomer() != null && Objects.equals(b.getCustomer().getId(), me.getId())) ||
                (me != null && b.getTechnician() != null && Objects.equals(b.getTechnician().getId(), me.getId())) ||
                hasTechAuthority();
        if (!allowed) return ResponseEntity.status(403).build();

        // mark booking complete
        b.setStatus("COMPLETED");
        bookingRepo.save(b);

        // create CompletedWork
        CompletedWork work = new CompletedWork();
        work.setTechnician(b.getTechnician());
        work.setCustomer(b.getCustomer());
        work.setServiceType(b.getServiceType());
        work.setSummary(payload != null ? Objects.toString(payload.get("summary"), null) : null);
        work.setAmount(payload != null && payload.get("amount") != null ? Double.valueOf(payload.get("amount").toString()) : null);
        work.setDistanceKm(payload != null && payload.get("distanceKm") != null ? Double.valueOf(payload.get("distanceKm").toString()) : null);
        work.setDurationMin(payload != null && payload.get("durationMin") != null ? Long.valueOf(payload.get("durationMin").toString()) : null);
        work.setStartedAt(payload != null && payload.get("startedAt") != null ? Instant.parse(payload.get("startedAt").toString()) : Instant.now());
        work.setFinishedAt(payload != null && payload.get("finishedAt") != null ? Instant.parse(payload.get("finishedAt").toString()) : Instant.now());

        completedWorkRepo.save(work);

        // notify via SSE
        LiveSnapshot s = snap(id);
        s.status = "COMPLETED";
        sendEvent(id, "status", Map.of("status", "COMPLETED", "bookingId", id));

        // return CompletedWork instead of Booking
        return ResponseEntity.ok(work);
    }

    @GetMapping(path = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable Long id) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        bucket(id).add(emitter);
        emitter.onCompletion(() -> bucket(id).remove(emitter));
        emitter.onTimeout(() -> bucket(id).remove(emitter));
        emitter.onError(ex -> bucket(id).remove(emitter));

        LiveSnapshot s = live.get(id);
        try {
            if (s != null) {
                if (s.status != null) {
                    emitter.send(SseEmitter.event().name("status")
                            .data(Map.of("status", s.status, "bookingId", id), MediaType.APPLICATION_JSON));
                }
                if (s.custLat != null && s.custLng != null) {
                    emitter.send(SseEmitter.event().name("location")
                            .data(Map.of("sender", "CUSTOMER", "lat", s.custLat, "lng", s.custLng, "ts", s.tsCust, "bookingId", id),
                                    MediaType.APPLICATION_JSON));
                }
                if (s.techLat != null && s.techLng != null) {
                    emitter.send(SseEmitter.event().name("location")
                            .data(Map.of("sender", "TECH", "lat", s.techLat, "lng", s.techLng, "ts", s.tsTech, "bookingId", id),
                                    MediaType.APPLICATION_JSON));
                }
            }
        } catch (IOException ignored) {}
        return emitter;
    }

    @PostMapping("/{id}/location")
    public ResponseEntity<?> postLocation(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body) {
        String email = currentEmailOrNull();
        if (email == null) return ResponseEntity.status(401).build();

        Booking b = bookingRepo.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();

        User me = userRepo.findByEmail(email);
        boolean allowed =
                (me != null && b.getCustomer() != null && Objects.equals(b.getCustomer().getId(), me.getId())) ||
                (me != null && b.getTechnician() != null && Objects.equals(b.getTechnician().getId(), me.getId())) ||
                hasTechAuthority();
        if (!allowed) return ResponseEntity.status(403).body("Not a participant of this booking.");

        String sender = Objects.toString(body.getOrDefault("sender", hasTechAuthority() ? "TECH" : "CUSTOMER")).toUpperCase(Locale.ROOT);
        Double lat = parseD(body.get("lat"));
        Double lng = parseD(body.get("lng"));
        long ts = System.currentTimeMillis();

        if (lat == null || lng == null) return ResponseEntity.badRequest().body("lat & lng required");

        LiveSnapshot s = snap(id);
        if ("TECH".equals(sender)) {
            s.techLat = lat; s.techLng = lng; s.tsTech = ts;
        } else {
            s.custLat = lat; s.custLng = lng; s.tsCust = ts;
        }

        Map<String, Object> payload = Map.of(
                "type", "location",
                "sender", sender,
                "lat", lat,
                "lng", lng,
                "ts", ts,
                "bookingId", id
        );
        sendEvent(id, "location", payload);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private static Double parseD(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return null; }
    }
}
