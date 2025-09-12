package com.example.demo.service;

import com.example.demo.dto.JobRequestDTO;
import com.example.demo.model.*;
import com.example.demo.repository.JobRequestRepository;
import com.example.demo.repository.TechnicianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobRequestService {

    private final JobRequestRepository repo;
    private final TechnicianRepository techRepo;

    public JobRequestService(JobRequestRepository repo, TechnicianRepository techRepo) {
        this.repo = repo;
        this.techRepo = techRepo;
    }

    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    /** Safe parser for "lat,lng". Returns true if both numbers were parsed. */
    private static boolean parseLatLng(String s, double[] out) {
        if (s == null) return false;
        String[] p = s.split(",");
        if (p.length != 2) return false;
        try {
            out[0] = Double.parseDouble(p[0].trim());
            out[1] = Double.parseDouble(p[1].trim());
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    /** Backward-compatible create used by older callers (no key supplied). */
    @Transactional
    public JobRequest create(JobRequestDTO dto) {
        return create(dto, null);
    }

    /** New overload: attaches a stable customerKey so “My Requests” works without phone. */
    @Transactional
    public JobRequest create(JobRequestDTO dto, String customerKey) {
        JobRequest j = new JobRequest();
        j.setTechnicianId(dto.technicianId);
        j.setMode(dto.mode);
        j.setStatus(JobStatus.PENDING);

        // ---- identity ----
        String resolvedKey = (customerKey != null && !customerKey.isBlank())
                ? customerKey
                : ("PHONE:" + (dto.customerPhone == null ? "" : dto.customerPhone));
        j.setCustomerKey(resolvedKey);

        // ---- customer info ----
        j.setCustomerName(dto.customerName);
        j.setCustomerPhone(dto.customerPhone);
        j.setCustomerAddress(dto.customerAddress);
        String loc = dto.customerLocation == null ? "" : dto.customerLocation.trim(); // never null (DB not-null)
        j.setCustomerLocation(loc);
        j.setNote(dto.note);
        j.setQuotedPrice(dto.quotedPrice);

        // ---- delivery fee (safe) ----
        // Only compute when technician travels; if any coordinate is missing/invalid -> fee = 0
        if (dto.mode == ServiceMode.I_COME_TO_YOU) {
            Technician t = techRepo.findById(dto.technicianId)
                    .orElseThrow(() -> new IllegalArgumentException("Technician not found: " + dto.technicianId));

            double[] techLL = new double[2];
            double[] custLL = new double[2];
            boolean tOk = parseLatLng(t.getLocation(), techLL);
            boolean cOk = parseLatLng(loc, custLL);

            if (tOk && cOk) {
                double km = distanceKm(techLL[0], techLL[1], custLL[0], custLL[1]);
                j.setDeliveryFee(Math.round(km * 10.0 * 100.0) / 100.0); // ₹10 per km
            } else {
                j.setDeliveryFee(0.0);
            }
        } else {
            j.setDeliveryFee(0.0);
        }

        return repo.save(j);
    }

    public List<JobRequest> listForTech(Long techId, JobStatus status) {
        return repo.findByTechnicianIdAndStatusOrderByCreatedAtDesc(techId, status);
    }

    public List<JobRequest> listForCustomer(String phone) {
        return repo.findByCustomerPhoneOrderByCreatedAtDesc(phone);
    }

    /** Fetch by stable customer key (no phone needed). */
    public List<JobRequest> listForKey(String customerKey) {
        return repo.findByCustomerKeyOrderByCreatedAtDesc(customerKey);
    }

    public JobRequest get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
    }

    @Transactional
    public JobRequest updateStatus(Long id, JobStatus status) {
        JobRequest j = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
        j.setStatus(status);
        return j; // JPA flushes on commit
    }
}
