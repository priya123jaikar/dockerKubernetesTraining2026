const API_BASE = "http://localhost:5000";

const productList = document.getElementById("product-list");
const statusEl = document.getElementById("status");
const form = document.getElementById("product-form");
const refreshBtn = document.getElementById("refresh");

async function loadProducts() {
  statusEl.textContent = "Loading products...";

  try {
    const response = await fetch(`${API_BASE}/api/products`);
    if (!response.ok) {
      throw new Error(`Request failed with ${response.status}`);
    }

    const products = await response.json();
    productList.innerHTML = "";

    if (products.length === 0) {
      productList.innerHTML = "<li>No products found.</li>";
    } else {
      products.forEach((product) => {
        const li = document.createElement("li");
        li.textContent = `#${product.id} ${product.name} - Qty: ${product.quantity}`;
        productList.appendChild(li);
      });
    }

    statusEl.textContent = "Products loaded.";
  } catch (error) {
    statusEl.textContent = `Error: ${error.message}`;
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const name = document.getElementById("name").value.trim();
  const quantity = Number(document.getElementById("quantity").value);

  if (!name || Number.isNaN(quantity)) {
    statusEl.textContent = "Please enter valid product details.";
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/api/products`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, quantity })
    });

    if (!response.ok) {
      throw new Error(`Request failed with ${response.status}`);
    }

    form.reset();
    statusEl.textContent = "Product added.";
    await loadProducts();
  } catch (error) {
    statusEl.textContent = `Error: ${error.message}`;
  }
});

refreshBtn.addEventListener("click", loadProducts);

loadProducts();
