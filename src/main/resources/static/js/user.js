// Customer profile data
customerProfile = {};

// Enhanced sample data for workers with location information

let currentService = '';
let currentWorker = null;
let selectedServiceOption = '';

// function redirectToService(serviceType) {
//     // Redirect using Thymeleaf routing
//     window.location.href = `/carpenter?service=${serviceType}`;
// }
function redirectToService(serviceType) {
    // Redirect dynamically to the correct service page
    if (serviceType === 'plumber') {
        window.location.href = `/plumber?service=${serviceType}`;
    } else if (serviceType === 'carpenter') {
        window.location.href = `/carpenter?service=${serviceType}`;
    } else if (serviceType === 'painter') {
        window.location.href = `/painter?service=${serviceType}`;
    } else if (serviceType === 'mechanic') {
        window.location.href = `/mechanic?service=${serviceType}`;
    } else if (serviceType === 'electrician') {
        window.location.href = `/electrician?service=${serviceType}`;
    } else if (serviceType === 'cleaner') {
        window.location.href = `/cleaner?service=${serviceType}`;
    } else if (serviceType === 'lawnmowing') {
        window.location.href = `/lawnmowing?service=${serviceType}`;
    } else if (serviceType === 'mason') {
        window.location.href = `/mason?service=${serviceType}`;
    } else {
        // fallback
        window.location.href = `/${serviceType}?service=${serviceType}`;
    }
}

async function showUserAddress() {
    const desktopSpan = document.getElementById('user-location');
    const mobileSpan = document.getElementById('mobile-user-location');

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(async (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            try {
                const res = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`);
                const data = await res.json();
                const address = data.address.city || data.address.town || data.address.village || data.display_name;

                // Update navbar for desktop & mobile
                desktopSpan.innerText = address;
                mobileSpan.innerText = address;

                // --- Send location to backend with real userId ---
                fetch('/api/save-user-location', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        userId: localStorage.getItem("loggedInUserId"), // ✅ fixed
                        latitude: lat,
                        longitude: lng,
                        fullAddress: address
                    })
                })
                .then(resp => resp.json())
                .then(data => console.log('User location saved:', data))
                .catch(err => console.error('Error saving user location:', err));
                // --- END ---

            } catch (err) {
                console.error("Error fetching address:", err);
                desktopSpan.innerText = "Location unavailable";
                mobileSpan.innerText = "Location unavailable";
            }
        }, (error) => {
            console.error("Location access denied:", error);
            desktopSpan.innerText = "Location access denied";
            mobileSpan.innerText = "Location access denied";
        });
    } else {
        desktopSpan.innerText = "Geolocation not supported";
        mobileSpan.innerText = "Geolocation not supported";
    }
}

// Run on page load
window.onload = showUserAddress;

// Calculate distance between two coordinates (Haversine formula)
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // Radius of the Earth in kilometers
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

// Update worker distances based on customer location
function updateWorkerDistances() {
    for (const service in workersData) {
        workersData[service].forEach(worker => {
            worker.distance = calculateDistance(
                customerProfile.latitude,
                customerProfile.longitude,
                worker.latitude,
                worker.longitude
            );
        });
    }
}

// Sort workers based on selected criteria
function sortWorkers(workers, sortBy) {
    return [...workers].sort((a, b) => {
        switch (sortBy) {
            case 'distance':
                return a.distance - b.distance;
            case 'rating':
                return b.rating - a.rating;
            case 'experience':
                return b.experience - a.experience;
            default:
                return a.distance - b.distance;
        }
    });
}

// Navigation functions
function showServiceWorkers(serviceType) {
    currentService = serviceType;
    updateWorkerDistances();

    const workers = workersData[serviceType] || [];
    const sortedWorkers = sortWorkers(workers, 'distance');

    // Update section title
    const serviceTitle = document.getElementById('service-title');
    serviceTitle.textContent = `${serviceType.charAt(0).toUpperCase() + serviceType.slice(1)} Services Near You`;

    // Populate workers grid
    populateWorkersGrid(sortedWorkers);

    // Hide services section and show service workers section
    hideAllSections();
    document.getElementById('service-workers').classList.remove('section-hidden');
    document.getElementById('service-workers').classList.add('section-visible');
}

function populateWorkersGrid(workers) {
    const workersGrid = document.getElementById('workers-grid');
    workersGrid.innerHTML = workers.map(worker => `
        <div class="worker-card bg-white rounded-lg shadow-lg p-6">
            <div class="flex items-center mb-4">
                <div class="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mr-4">
                    <i class="fas fa-user text-purple-600 text-2xl"></i>
                </div>
                <div class="flex-1">
                    <div class="flex items-center justify-between mb-1">
                        <h3 class="text-xl font-semibold">${worker.name}</h3>
                        <span class="distance-badge">${worker.distance.toFixed(1)} km</span>
                    </div>
                    <div class="star-rating mb-1">
                        ${generateStars(worker.rating)}
                    </div>
                    <p class="text-sm text-gray-600">${worker.experience} years experience</p>
                </div>
            </div>
            <p class="text-gray-600 mb-3">${worker.description}</p>
            <div class="mb-4">
                <span class="location-badge">
                    <i class="fas fa-map-marker-alt mr-1"></i>${worker.availability}
                </span>
            </div>
            <div class="mb-4">
                <p class="text-sm font-medium text-gray-700">
                    <i class="fas fa-phone mr-2"></i>${worker.phone}
                </p>
                <p class="text-sm text-gray-600">
                    <i class="fas fa-map-marker-alt mr-2"></i>${worker.location}
                </p>
            </div>
            <button onclick="showWorkerDetails('${worker.id}')" class="w-full bg-purple-600 text-white py-2 px-4 rounded-lg font-semibold hover:bg-purple-700 transition duration-300">
                View Details
            </button>
        </div>
    `).join('');
}

function goBackToServices() {
    hideAllSections();
    showMainSections();
}

function hideAllSections() {
    const sections = ['home', 'about', 'services', 'service-workers', 'how-it-works', 'reviews', 'help', 'profile'];
    sections.forEach(section => {
        const element = document.getElementById(section);
        if (element) {
            element.classList.add('section-hidden');
            element.classList.remove('section-visible');
        }
    });
}

function showMainSections() {
    const sections = ['home', 'about', 'services', 'how-it-works', 'reviews', 'help', 'profile'];
    sections.forEach(section => {
        const element = document.getElementById(section);
        if (element) {
            element.classList.remove('section-hidden');
            element.classList.add('section-visible');
        }
    });
}

function showWorkerDetails(workerId) {
    let worker = null;
    for (const service in workersData) {
        worker = workersData[service].find(w => w.id === workerId);
        if (worker) break;
    }

    if (!worker) return;

    currentWorker = worker;
    const workerProfile = document.getElementById('worker-profile');
    workerProfile.innerHTML = `
        <div class="grid md:grid-cols-3 gap-8 mb-8">
            <div class="md:col-span-1">
                <div class="text-center">
                    <div class="w-32 h-32 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <i class="fas fa-user text-purple-600 text-4xl"></i>
                    </div>
                    <h2 class="text-2xl font-bold mb-2">${worker.name}</h2>
                    <div class="star-rating justify-center mb-2">
                        ${generateStars(worker.rating)}
                    </div>
                    <p class="text-gray-600 mb-2">${worker.experience} years experience</p>
                    <span class="location-badge">${worker.availability}</span>
                    <div class="text-left space-y-2 mt-4">
                        <p class="text-sm">
                            <i class="fas fa-phone w-4 mr-2 text-purple-600"></i>${worker.phone}
                        </p>
                        <p class="text-sm">
                            <i class="fas fa-map-marker-alt w-4 mr-2 text-purple-600"></i>${worker.location}
                        </p>
                        <p class="text-sm">
                            <i class="fas fa-road w-4 mr-2 text-green-600"></i>${worker.distance.toFixed(1)} km away
                        </p>
                    </div>
                </div>
            </div>
            <div class="md:col-span-2">
                <div class="mb-6">
                    <h3 class="text-xl font-semibold mb-3">About</h3>
                    <p class="text-gray-600 mb-4">${worker.description}</p>
                    <p class="text-gray-600">${worker.portfolio}</p>
                </div>
                <div class="mb-6">
                    <h3 class="text-xl font-semibold mb-3">Services Offered</h3>
                    <div class="flex flex-wrap gap-2">
                        ${worker.services.map(service => `
                            <span class="bg-purple-100 text-purple-800 px-3 py-1 rounded-full text-sm">${service}</span>
                        `).join('')}
                    </div>
                </div>
                <div class="mb-8">
                    <h3 class="text-xl font-semibold mb-3">Service Options</h3>
                    <div class="grid md:grid-cols-2 gap-4">
                        <button onclick="selectServiceOption('you_come_to_me')" class="service-option-card p-4 rounded-lg hover:bg-purple-50 transition duration-300 text-left">
                            <i class="fas fa-home text-purple-600 text-2xl mb-2"></i>
                            <h4 class="font-semibold">I come to you</h4>
                            <p class="text-sm text-gray-600">You visit technician's workshop/location</p>
                        </button>
                        <button onclick="selectServiceOption('i_come_to_you')" class="service-option-card p-4 rounded-lg hover:bg-purple-50 transition duration-300 text-left">
                            <i class="fas fa-tools text-purple-600 text-2xl mb-2"></i>
                            <h4 class="font-semibold">You come to me</h4>
                            <p class="text-sm text-gray-600">Technician visits your location (Door service)</p>
                        </button>
                    </div>
                    <div id="cancel-button-container" class="hidden mt-4">
                        <button onclick="cancelBooking()" class="w-full bg-red-500 text-white py-3 rounded-lg font-semibold hover:bg-red-600 transition duration-300">
                            Cancel Booking
                        </button>
                    </div>
                </div>
                <div>
                    <h3 class="text-xl font-semibold mb-3">Recent Reviews</h3>
                    <div class="space-y-4">
                        ${worker.reviews.map(review => `
                            <div class="border-l-4 border-purple-200 pl-4">
                                <div class="flex items-center mb-1">
                                    <span class="font-medium mr-2">${review.name}</span>
                                    <div class="star-rating">
                                        ${generateStars(review.rating)}
                                    </div>
                                </div>
                                <p class="text-gray-600 text-sm">${review.comment}</p>
                            </div>
                        `).join('')}
                    </div>
                </div>
            </div>
        </div>
    `;

    document.getElementById('worker-details').classList.add('active');
}

function hideWorkerDetails() {
    document.getElementById('worker-details').classList.remove('active');
    selectedServiceOption = '';
    document.getElementById('cancel-button-container').classList.add('hidden');
}

function selectServiceOption(option) {
    selectedServiceOption = option;

    if (option === 'you_come_to_me') {
        showTechnicianLocation();
    } else if (option === 'i_come_to_you') {
        showLocationModal();
        document.getElementById('cancel-button-container').classList.remove('hidden');
    }
}

function showTechnicianLocation() {
    const techLocationInfo = document.getElementById('technician-location-info');
    techLocationInfo.innerHTML = `
        <div class="bg-gray-50 rounded-lg p-4 mb-4">
            <h4 class="font-semibold mb-2">${currentWorker.name}'s Workshop</h4>
            <p class="text-gray-600 mb-2">
                <i class="fas fa-map-marker-alt mr-2"></i>${currentWorker.workshopAddress}
            </p>
            <p class="text-gray-600 mb-2">
                <i class="fas fa-phone mr-2"></i>${currentWorker.phone}
            </p>
            <p class="text-sm text-purple-600">
                <i class="fas fa-road mr-2"></i>Distance: ${currentWorker.distance.toFixed(1)} km
            </p>
        </div>
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-3">
            <p class="text-blue-800 text-sm">
                <i class="fas fa-info-circle mr-2"></i>
                Please call ahead to confirm availability and discuss your problem.
            </p>
        </div>
    `;

    document.getElementById('technician-location-modal').classList.add('active');
}

function closeTechnicianLocationModal() {
    document.getElementById('technician-location-modal').classList.remove('active');
}

function showLocationModal() {
    document.getElementById('location-modal').classList.add('active');
}

function closeLocationModal() {
    document.getElementById('location-modal').classList.remove('active');
    if (selectedServiceOption === 'i_come_to_you') {
        document.getElementById('cancel-button-container').classList.add('hidden');
        selectedServiceOption = '';
    }
}

function cancelBooking() {
    showNotification(`${currentWorker.name} has been notified that you cancelled the service request.`);
    selectedServiceOption = '';
    document.getElementById('cancel-button-container').classList.add('hidden');
    hideWorkerDetails();
}

function showNotification(message) {
    const notificationText = document.getElementById('notification-text');
    const notificationPopup = document.getElementById('notification-popup');

    notificationText.textContent = message;
    notificationPopup.classList.add('show');

    setTimeout(() => {
        hideNotification();
    }, 5000);
}

function hideNotification() {
    document.getElementById('notification-popup').classList.remove('show');
}

// ------------------ Profile Management ------------------

// These MUST be set by your login page after successful login
let loggedInEmail  = localStorage.getItem("loggedInEmail")  || "";
let loggedInUserId = localStorage.getItem("loggedInUserId") || "";

// Always read the latest token if you later add JWT
function makeHeaders() {
  const token = localStorage.getItem("authToken") || localStorage.getItem("token") || "";
  const h = { "Content-Type": "application/json" };
  if (token) h["Authorization"] = `Bearer ${token}`;
  return h;
}

function showInlineError(msg) {
  console.error("[Profile] " + msg);
  let host = document.getElementById("profile-error-banner");
  if (!host) {
    host = document.createElement("div");
    host.id = "profile-error-banner";
    host.style.margin = "0 0 12px 0";
    host.innerHTML =
      '<div style="background:#fee2e2;color:#991b1b;border:1px solid #fecaca;padding:12px;border-radius:8px;font-weight:600;">' +
      '<span>⚠️ Profile error: </span><span id="profile-error-text"></span>' +
      '</div>';
    const card = document.querySelector("#profile .bg-white.rounded-lg") || document.body;
    card.prepend(host);
  }
  const text = document.getElementById("profile-error-text");
  if (text) text.textContent = msg;
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}
function setValue(id, value) {
  const el = document.getElementById(id);
  if (el) el.value = value;
}
function show(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('hidden');
}
function hide(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('hidden');
}

function fillProfileUI(user) {
  const safe = (v) => (v === null || v === undefined ? "" : String(v));

  setText("profile-display-name", (`${safe(user.firstName)} ${safe(user.lastName)}`).trim() || "User");

  setText("display-firstname", safe(user.firstName));
  setText("display-lastname",  safe(user.lastName));
  setText("display-phone",     safe(user.phone));
  setText("display-location",  safe(user.location));
  setText("display-email",     safe(user.email));

  setValue("edit-firstname", safe(user.firstName));
  setValue("edit-lastname",  safe(user.lastName));
  setValue("edit-phone",     safe(user.phone));
  setValue("edit-location",  safe(user.location));

  if (user.id) {
    loggedInUserId = user.id;
    localStorage.setItem("loggedInUserId", String(user.id));
  }
  if (user.email) {
    loggedInEmail = user.email;
    localStorage.setItem("loggedInEmail", user.email);
  }

  // show display, hide edit
  hide('profile-edit');
  show('profile-display');
}

async function loadProfileStrict() {
  if (!loggedInEmail) {
    showInlineError("No loggedInEmail found. Please login again.");
    alert("Please login again.");
    window.location.href = "/login-user";
    return;
  }
  const res = await fetch(`/users/email?email=${encodeURIComponent(loggedInEmail)}`, {
    headers: makeHeaders()
  });
  if (!res.ok) {
    showInlineError(`GET /users/email failed (HTTP ${res.status}).`);
    throw new Error("Profile load failed");
  }
  const user = await res.json();
  if (!user || (!user.id && !user.email)) {
    showInlineError("Invalid user payload from /users/email.");
    throw new Error("Invalid user payload");
  }
  fillProfileUI(user);
}

// UI toggles (called by buttons in HTML)
function editProfile() {
  hide('profile-display');
  show('profile-edit');
}
function cancelEdit() {
  hide('profile-edit');
  show('profile-display');
}

async function saveProfileUpdate(e) {
  e.preventDefault();

  // Ensure we have the id
  if (!loggedInUserId) {
    // try to re-load via email to populate id
    await loadProfileStrict().catch(() => {});
  }
  if (!loggedInUserId) {
    showInlineError("No user id available to update. Please login again.");
    alert("Please login again.");
    window.location.href = "/login-user";
    return;
  }

  const payload = {
    firstName: (document.getElementById("edit-firstname")?.value || "").trim(),
    lastName:  (document.getElementById("edit-lastname")?.value  || "").trim(),
    phone:     (document.getElementById("edit-phone")?.value     || "").trim(),
    location:  (document.getElementById("edit-location")?.value  || "").trim()
  };

  const res = await fetch(`/users/${encodeURIComponent(loggedInUserId)}/profile`, {
    method: "PUT",
    headers: makeHeaders(),
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    showInlineError(`PUT /users/${loggedInUserId}/profile failed (HTTP ${res.status}).`);
    alert("Update failed.");
    return;
  }
  const updated = await res.json();
  fillProfileUI(updated);
  alert("Profile updated successfully!");
  cancelEdit();
}

// Init
document.addEventListener("DOMContentLoaded", () => {
  // Ensure correct initial view
  hide('profile-edit');
  show('profile-display');

  const form = document.getElementById("profile-form");
  if (form) form.addEventListener("submit", saveProfileUpdate);

  // ✅ FIX: correct profile load call
  loadProfileStrict().catch(err => {
    console.error("Profile load failed:", err);
    showInlineError("Could not load profile (check /users/email).");
  });

  // Chatbot input (kept as-is)
  const chatbotInput = document.getElementById('chatbot-input');
  if (chatbotInput) {
    chatbotInput.addEventListener('keypress', function (e) {
      if (e.key === 'Enter') sendMessage();
    });
  }
});

// ------------------ Chatbot Functions ------------------

function toggleChatbot() {
    const chatbotWindow = document.getElementById('chatbot-window');
    chatbotWindow.classList.toggle('active');
}

function sendMessage() {
    const input = document.getElementById('chatbot-input');
    const message = input.value.trim();

    if (!message) return;

    addChatMessage(message, 'user');
    input.value = '';

    // Simple chatbot responses
    setTimeout(() => {
        const response = getChatbotResponse(message);
        addChatMessage(response, 'bot');
    }, 1000);
}

function addChatMessage(message, sender) {
    const messagesContainer = document.getElementById('chatbot-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}`;
    messageDiv.innerHTML = `<p>${message}</p>`;
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function getChatbotResponse(message) {
    const lowerMessage = message.toLowerCase();

    if (lowerMessage.includes('service') || lowerMessage.includes('services')) {
        return "We offer various services including carpentry, plumbing, electrical work, painting, lawn mowing, masonry, and cleaning. Which service are you interested in?";
    } else if (lowerMessage.includes('price') || lowerMessage.includes('cost')) {
        return "Prices vary by service and location. Each technician sets their own rates. You can contact them directly to discuss pricing for your specific needs.";
    } else if (lowerMessage.includes('available') || lowerMessage.includes('availability')) {
        return "Most of our technicians are available today or tomorrow. You can check their availability when viewing their profiles.";
    } else {
        return "I'm here to help! Can you please provide more details about your request?";
    }
}

// ------------------ Utility Functions ------------------

function generateStars(rating) {
    let starsHTML = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            starsHTML += '<i class="fas fa-star text-yellow-400"></i>';
        } else {
            starsHTML += '<i class="far fa-star text-gray-300"></i>';
        }
    }
    return starsHTML;
}
