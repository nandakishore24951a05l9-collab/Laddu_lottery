// cart.js

// Add item to cart
function addToCart(service) {
  let cart = JSON.parse(localStorage.getItem("cart")) || [];
  cart.push(service);
  localStorage.setItem("cart", JSON.stringify(cart));
  alert(`${service} added to cart ✅`);
}

// Load cart items
function loadCart() {
  let cart = JSON.parse(localStorage.getItem("cart")) || [];
  const list = document.getElementById("cart-list");
  if (!list) return;

  list.innerHTML = "";
  cart.forEach((item, index) => {
    list.innerHTML += `
      <li class="flex justify-between bg-gray-100 p-2 rounded-md">
        <span>${item}</span>
        <button onclick="removeFromCart(${index})" class="text-red-500">❌</button>
      </li>
    `;
  });
}

// Remove item
function removeFromCart(index) {
  let cart = JSON.parse(localStorage.getItem("cart")) || [];
  cart.splice(index, 1);
  localStorage.setItem("cart", JSON.stringify(cart));
  loadCart();
}

// Checkout
function checkout() {
  alert("Proceeding to checkout 🛒");
  localStorage.removeItem("cart");
  loadCart();
}

// Run automatically if cart.html is loaded
document.addEventListener("DOMContentLoaded", loadCart);
