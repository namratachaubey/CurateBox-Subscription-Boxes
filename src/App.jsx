import React, { useEffect, useState } from "react"

const API = "http://localhost:8080/api"

export default function App() {
  const [token, setToken] = useState(localStorage.getItem("token") || "")
  const [user, setUser] = useState(
    JSON.parse(localStorage.getItem("user") || "null")
  )
  const [tab, setTab] = useState("discover")
  const [message, setMessage] = useState("")

  // Admin dashboard state
  const [adminSection, setAdminSection] = useState("overview")
  const [adminOverview, setAdminOverview] = useState(null)
  const [adminProducts, setAdminProducts] = useState([])
  const [adminCustomers, setAdminCustomers] = useState([])
  const [adminSubscriptions, setAdminSubscriptions] = useState([])
  const [adminOrders, setAdminOrders] = useState([])
  const [adminRatings, setAdminRatings] = useState([])
  const [adminLoading, setAdminLoading] = useState(false)

  const [adminCategoryName, setAdminCategoryName] = useState("")
  const [adminCategoryDescription, setAdminCategoryDescription] = useState("")
  const [adminProduct, setAdminProduct] = useState({
    name: "",
    description: "",
    price: "",
    tags: "",
    categoryId: "",
  })

  // Customer state
  const [categories, setCategories] = useState([])
  const [products, setProducts] = useState([])
  const [recommendations, setRecommendations] = useState([])
  const [orders, setOrders] = useState([])
  const [subs, setSubs] = useState([])
  const [ratings, setRatings] = useState([])

  const [authMode, setAuthMode] = useState("login")
  const [auth, setAuth] = useState({
    name: "",
    email: "",
    password: "",
  })

  const [pref, setPref] = useState({
    categoryIds: [],
    monthlyBudget: "30",
    interests: "",
    avoidItems: "",
  })
  const [plan, setPlan] = useState({
    planName: "My Monthly Box",
    categoryId: "",
    monthlyBudget: "30",
  })

  const [rate, setRate] = useState({
    productId: "",
    score: "5",
    comment: "",
  })

  const isAdmin = user?.role === "ADMIN"
  const isCustomer = Boolean(token && user?.role === "CUSTOMER")

  const headers = () => ({
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  })

  async function api(path, opts = {}) {
    const res = await fetch(API + path, {
      ...opts,
      headers: { ...headers(), ...(opts.headers || {}) },
    })

    if (!res.ok) {
      let msg = "Request failed"

      try {
        const errorData = await res.json()
        msg = errorData.message || msg
      } catch {}

      throw new Error(msg)
    }

    if (res.status === 204) return null

    const text = await res.text()
    if (!text.trim()) return null

    try {
      return JSON.parse(text)
    } catch {
      throw new Error("Invalid JSON response from " + path)
    }
  }

  const money = (n) => `$${Number(n || 0).toFixed(2)}`

  function showError(prefix, error) {
    setMessage(prefix + ": " + error.message)
  }

  // Public catalogue
  async function loadPublic() {
    try {
      setCategories(await api("/categories"))
      setProducts(await api("/products"))
    } catch {
      setMessage(
        "Could not connect to backend. Start Spring Boot and MySQL first."
      )
    }
  }

  // Customer data
  async function loadPrivate() {
    try {
      setRecommendations(await api("/recommendations"))
      setOrders(await api("/orders"))
      setSubs(await api("/subscriptions"))
      setRatings(await api("/ratings"))

      const p = await api("/preferences")

      if (p) {
        const savedCategories =
          Array.isArray(p.categories) && p.categories.length > 0
            ? p.categories
            : p.category
            ? [p.category]
            : []

        setPref({
          categoryIds: savedCategories.map((c) => String(c.id)),
          monthlyBudget: String(p.monthlyBudget),
          interests: p.interests || "",
          avoidItems: p.avoidItems || "",
        })

        setPlan((v) => ({
          ...v,
          categoryId: p.category?.id
            ? String(p.category.id)
            : savedCategories.length > 0
            ? String(savedCategories[0].id)
            : v.categoryId,
          monthlyBudget: String(p.monthlyBudget),
        }))
      }
    } catch (e) {
      setMessage(e.message)
    }
  }

  // Admin data loaders
  async function loadAdminOverview() {
    try {
      setAdminOverview(await api("/admin/overview"))
    } catch (e) {
      showError("Could not load overview", e)
    }
  }

  async function loadAdminCustomers() {
    try {
      setAdminCustomers(await api("/admin/customers"))
    } catch (e) {
      showError("Could not load customers", e)
    }
  }

  async function loadAdminSubscriptions() {
    try {
      setAdminSubscriptions(await api("/admin/subscriptions"))
    } catch (e) {
      showError("Could not load subscriptions", e)
    }
  }

  async function loadAdminOrders() {
    try {
      setAdminOrders(await api("/admin/orders"))
    } catch (e) {
      showError("Could not load orders", e)
    }
  }

  async function loadAdminRatings() {
    try {
      setAdminRatings(await api("/admin/ratings"))
    } catch (e) {
      showError("Could not load ratings", e)
    }
  }

  async function loadAdminProducts() {
    try {
      setAdminProducts(await api("/products"))
    } catch (e) {
      showError("Could not load products", e)
    }
  }

  async function loadAdminSection(section) {
    setAdminLoading(true)
    setAdminSection(section)

    try {
      if (section === "overview") {
        await loadAdminOverview()
      } else if (section === "customers") {
        await loadAdminCustomers()
      } else if (section === "subscriptions") {
        await loadAdminSubscriptions()
      } else if (section === "orders") {
        await loadAdminOrders()
      } else if (section === "ratings") {
        await loadAdminRatings()
      } else if (section === "products") {
        await loadAdminProducts()
        await loadPublic()
      }
    } finally {
      setAdminLoading(false)
    }
  }

  async function refreshAdminSection() {
    await loadAdminSection(adminSection)
  }

  // Add a category
  async function addAdminCategory(e) {
    e.preventDefault()

    try {
      await api("/admin/categories", {
        method: "POST",
        body: JSON.stringify({
          name: adminCategoryName,
          description: adminCategoryDescription,
        }),
      })

      setAdminCategoryName("")
      setAdminCategoryDescription("")
      await loadPublic()
      setMessage("Category added successfully.")
    } catch (e) {
      showError("Could not add category", e)
    }
  }

  // Add a product
  async function addAdminProduct(e) {
    e.preventDefault()

    try {
      await api("/admin/products", {
        method: "POST",
        body: JSON.stringify({
          ...adminProduct,
          price: Number(adminProduct.price),
          categoryId: Number(adminProduct.categoryId),
        }),
      })

      setAdminProduct({
        name: "",
        description: "",
        price: "",
        tags: "",
        categoryId: "",
      })

      await loadPublic()
      await loadAdminProducts()
      setMessage("Product added successfully.")
    } catch (e) {
      showError("Could not add product", e)
    }
  }

  // Deactivate a product
  async function deactivateAdminProduct(id) {
    try {
      await api(`/admin/products/${id}/deactivate`, {
        method: "PUT",
      })

      await loadAdminProducts()
      await loadPublic()
      setMessage("Product deactivated successfully.")
    } catch (e) {
      showError("Could not deactivate product", e)
    }
  }

  useEffect(() => {
    loadPublic()
  }, [])

  useEffect(() => {
    if (isCustomer) {
      loadPrivate()
    }
  }, [token, user?.role])

  // Login and registration
  async function submitAuth(e) {
    e.preventDefault()

    try {
      const path = authMode === "login" ? "/auth/login" : "/auth/register"

      const result = await api(path, {
        method: "POST",
        body: JSON.stringify(auth),
      })

      setToken(result.token)
      setUser(result)

      localStorage.setItem("token", result.token)
      localStorage.setItem("user", JSON.stringify(result))

      setMessage("Welcome, " + result.name + "!")

      if (result.role === "ADMIN") {
        setTab("admin")
        setAdminSection("overview")
        await loadPublic()
        await loadAdminOverview()
        await loadAdminProducts()
      } else {
        setTab("discover")
      }
    } catch (e) {
      setMessage(
        authMode === "login"
          ? "Login failed. Please check your email and password, or register for a new account."
          : e.message
      )
    }
  }

  function logout() {
    setToken("")
    setUser(null)
    setAdminOverview(null)
    setAdminProducts([])
    setAdminCustomers([])
    setAdminSubscriptions([])
    setAdminOrders([])
    setAdminRatings([])
    setRecommendations([])
    setOrders([])
    setSubs([])
    setRatings([])

    localStorage.removeItem("token")
    localStorage.removeItem("user")

    setTab("discover")
    setMessage("You have logged out.")
  }

  async function savePref(e) {
    e.preventDefault()

    if (pref.categoryIds.length === 0) {
      setMessage("Please select at least one category.")
      return
    }

    try {
      await api("/preferences", {
        method: "POST",
        body: JSON.stringify({
          categoryIds: pref.categoryIds.map(Number),
          categoryId: Number(pref.categoryIds[0]),
          monthlyBudget: Number(pref.monthlyBudget),
          interests: pref.interests,
          avoidItems: pref.avoidItems,
        }),
      })

      await loadPrivate()
      setMessage("Preferences saved. Recommendations updated.")
    } catch (e) {
      setMessage(e.message)
    }
  }

  async function createPlan(e) {
    e.preventDefault()

    try {
      await api("/subscriptions", {
        method: "POST",
        body: JSON.stringify({
          ...plan,
          categoryId: Number(plan.categoryId),
          monthlyBudget: Number(plan.monthlyBudget),
        }),
      })

      await loadPrivate()
      setTab("orders")
      setMessage("Subscription created and your first box generated!")
    } catch (e) {
      setMessage(e.message)
    }
  }

  async function submitRating(e) {
    e.preventDefault()

    try {
      await api("/ratings", {
        method: "POST",
        body: JSON.stringify({
          ...rate,
          productId: Number(rate.productId),
          score: Number(rate.score),
        }),
      })

      await loadPrivate()
      setMessage("Rating saved. Future recommendations will use it.")
    } catch (e) {
      setMessage(e.message)
    }
  }

  async function cancel(id) {
    try {
      await api(`/subscriptions/${id}/cancel`, {
        method: "PUT",
      })

      await loadPrivate()
      setMessage("Subscription cancelled.")
    } catch (e) {
      setMessage(e.message)
    }
  }

  // Prevent customers and visitors from opening admin-only screens
  function openTab(nextTab) {
    if (nextTab === "admin") {
      if (isAdmin) {
        setTab("admin")
        loadAdminSection("overview")
      }
      return
    }

    if (
      ["quiz", "subscriptions", "orders", "ratings"].includes(nextTab) &&
      !isCustomer
    ) {
      setTab("auth")
      setAuthMode("login")
      return
    }

    if (isAdmin) return

    setTab(nextTab)
  }

  return (
    <div className="app">
      <header>
        <div className="brand">
          <span className="logo">B</span>
          <div>
            <b>CurateBox</b>
            <small>YOUR MONTHLY DISCOVERY</small>
          </div>
        </div>

        {/* Role-based navigation */}
        <nav>
          {isAdmin ? (
            <button
              className={tab === "admin" ? "active" : ""}
              onClick={() => openTab("admin")}
            >
              Admin Dashboard
            </button>
          ) : (
            <>
              <button
                className={tab === "discover" ? "active" : ""}
                onClick={() => openTab("discover")}
              >
                Discover
              </button>

              {isCustomer && (
                <>
                  <button
                    className={tab === "quiz" ? "active" : ""}
                    onClick={() => openTab("quiz")}
                  >
                    Preference quiz
                  </button>
                  <button
                    className={tab === "subscriptions" ? "active" : ""}
                    onClick={() => openTab("subscriptions")}
                  >
                    Subscriptions
                  </button>
                  <button
                    className={tab === "orders" ? "active" : ""}
                    onClick={() => openTab("orders")}
                  >
                    My boxes
                  </button>
                  <button
                    className={tab === "ratings" ? "active" : ""}
                    onClick={() => openTab("ratings")}
                  >
                    Ratings
                  </button>
                </>
              )}
            </>
          )}
        </nav>

        <div className="account">
          {user ? (
            <>
              <span>{user.name}</span>
              <button className="outline" onClick={logout}>
                Log out
              </button>
            </>
          ) : (
            <button
              className="primary small"
              onClick={() => {
                setTab("auth")
                setAuthMode("login")
              }}
            >
              Sign in
            </button>
          )}
        </div>
      </header>

      <main>
        {/* ADMIN DASHBOARD */}
        {isAdmin && tab === "admin" && (
          <section className="panel">
            <span className="eyebrow">BOXLY ADMINISTRATION</span>
            <h2>Admin Dashboard</h2>
            <p>
              Monitor customers, subscriptions, orders, ratings and products.
            </p>

            <div className="admin-menu">
              {[
                ["overview", "Overview"],
                ["customers", "Customers"],
                ["subscriptions", "Subscriptions"],
                ["orders", "Orders"],
                ["ratings", "Ratings & Feedback"],
                ["products", "Products & Categories"],
              ].map(([id, label]) => (
                <button
                  key={id}
                  type="button"
                  className={adminSection === id ? "primary" : "outline"}
                  onClick={() => loadAdminSection(id)}
                >
                  {label}
                </button>
              ))}
            </div>

            <button
              type="button"
              className="outline"
              onClick={refreshAdminSection}
              disabled={adminLoading}
            >
              {adminLoading ? "Refreshing..." : "Refresh Current Section"}
            </button>

            {adminSection === "overview" && (
              <div>
                <h3>Platform Overview</h3>

                <div className="admin-stats">
                  {adminOverview ? (
                    <>
                      <div className="panel">
                        <h3>Total Customers</h3>
                        <h2>{adminOverview.totalCustomers}</h2>
                      </div>
                      <div className="panel">
                        <h3>Active Subscriptions</h3>
                        <h2>{adminOverview.activeSubscriptions}</h2>
                      </div>
                      <div className="panel">
                        <h3>Total Orders</h3>
                        <h2>{adminOverview.totalOrders}</h2>
                      </div>
                      <div className="panel">
                        <h3>Total Order Value</h3>
                        <h2>{money(adminOverview.totalOrderValue)}</h2>
                      </div>
                      <div className="panel">
                        <h3>Average Rating</h3>
                        <h2>
                          {Number(adminOverview.averageRating || 0).toFixed(1)}{" "}
                          / 5
                        </h2>
                      </div>
                      <div className="panel">
                        <h3>Total Ratings</h3>
                        <h2>{adminOverview.totalRatings}</h2>
                      </div>
                    </>
                  ) : (
                    <p>
                      No overview data loaded yet. Use Refresh Current Section.
                    </p>
                  )}
                </div>
              </div>
            )}

            {adminSection === "customers" && (
              <div>
                <h3>Customer Management</h3>
                <p>Registered customers and their saved preferences.</p>

                {adminCustomers.length === 0 ? (
                  <p>No customers found, or the list has not been loaded.</p>
                ) : (
                  <div className="list">
                    {adminCustomers.map((c) => (
                      <div className="list-item" key={c.id}>
                        <div>
                          <b>{c.name}</b>
                          <p>Email: {c.email}</p>
                          <p>
                            Registered:{" "}
                            {c.registeredAt
                              ? new Date(c.registeredAt).toLocaleString()
                              : "Not available"}
                          </p>
                          <p>Category: {c.preferenceCategory || "Not set"}</p>
                          <p>
                            Monthly budget:{" "}
                            {c.preferenceBudget != null
                              ? money(c.preferenceBudget)
                              : "Not set"}
                          </p>
                          <p>Interests: {c.interests || "Not provided"}</p>
                          <p>Avoid items: {c.avoidItems || "None specified"}</p>
                          <p>Active subscriptions: {c.activeSubscriptions}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {adminSection === "subscriptions" && (
              <div>
                <h3>Subscription Management</h3>
                <p>Review customer plans and subscription status.</p>

                {adminSubscriptions.length === 0 ? (
                  <p>
                    No subscriptions found, or the list has not been loaded.
                  </p>
                ) : (
                  <div className="list">
                    {adminSubscriptions.map((s) => (
                      <div className="list-item" key={s.id}>
                        <div>
                          <b>{s.planName}</b>
                          <p>Customer: {s.customerName}</p>
                          <p>Email: {s.customerEmail}</p>
                          <p>Category: {s.category}</p>
                          <p>Monthly budget: {money(s.monthlyBudget)}</p>
                          <p>
                            Next order: {s.nextOrderDate || "Not scheduled"}
                          </p>
                        </div>
                        <span className={s.active ? "status" : "status off"}>
                          {s.active ? "Active" : "Cancelled"}
                        </span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {adminSection === "orders" && (
              <div>
                <h3>Order Management</h3>
                <p>Review orders, products, dates and order statuses.</p>

                {adminOrders.length === 0 ? (
                  <p>No orders found, or the list has not been loaded.</p>
                ) : (
                  <div className="list">
                    {adminOrders.map((o) => (
                      <div className="list-item" key={o.id}>
                        <div>
                          <b>Order #{o.id}</b>
                          <p>Customer: {o.customerName}</p>
                          <p>Plan: {o.planName}</p>
                          <p>Date: {o.orderDate || "Not available"}</p>
                          <p>
                            Products:{" "}
                            {o.products?.length
                              ? o.products.join(", ")
                              : "No products"}
                          </p>
                          <p>Total: {money(o.total)}</p>
                        </div>
                        <span className="status">{o.status}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {adminSection === "ratings" && (
              <div>
                <h3>Ratings & Feedback</h3>
                <p>See customer reviews and product ratings.</p>

                {adminRatings.length === 0 ? (
                  <p>No ratings found, or the list has not been loaded.</p>
                ) : (
                  <div className="list">
                    {adminRatings.map((r) => (
                      <div className="list-item" key={r.id}>
                        <div>
                          <b>{r.productName}</b>
                          <p>Customer: {r.customerName}</p>
                          <p>
                            Rating: {"★".repeat(r.score)}
                            {"☆".repeat(Math.max(0, 5 - r.score))} ({r.score}/5)
                          </p>
                          <p>Feedback: {r.comment || "No written feedback"}</p>
                          <p>
                            Submitted:{" "}
                            {r.ratedAt
                              ? new Date(r.ratedAt).toLocaleString()
                              : "Not available"}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {adminSection === "products" && (
              <div>
                <h3>Add Category</h3>
                <form className="stack" onSubmit={addAdminCategory}>
                  <input
                    placeholder="Category name"
                    required
                    value={adminCategoryName}
                    onChange={(e) => setAdminCategoryName(e.target.value)}
                  />
                  <input
                    placeholder="Category description"
                    value={adminCategoryDescription}
                    onChange={(e) =>
                      setAdminCategoryDescription(e.target.value)
                    }
                  />
                  <button className="primary" type="submit">
                    Add Category
                  </button>
                </form>

                <h3>Add Product</h3>
                <form className="stack" onSubmit={addAdminProduct}>
                  <input
                    placeholder="Product name"
                    required
                    value={adminProduct.name}
                    onChange={(e) =>
                      setAdminProduct({ ...adminProduct, name: e.target.value })
                    }
                  />
                  <input
                    placeholder="Product description"
                    value={adminProduct.description}
                    onChange={(e) =>
                      setAdminProduct({
                        ...adminProduct,
                        description: e.target.value,
                      })
                    }
                  />
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    placeholder="Price"
                    required
                    value={adminProduct.price}
                    onChange={(e) =>
                      setAdminProduct({
                        ...adminProduct,
                        price: e.target.value,
                      })
                    }
                  />
                  <input
                    placeholder="Tags"
                    value={adminProduct.tags}
                    onChange={(e) =>
                      setAdminProduct({ ...adminProduct, tags: e.target.value })
                    }
                  />
                  <select
                    required
                    value={adminProduct.categoryId}
                    onChange={(e) =>
                      setAdminProduct({
                        ...adminProduct,
                        categoryId: e.target.value,
                      })
                    }
                  >
                    <option value="">Choose category</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                  <button className="primary" type="submit">
                    Add Product
                  </button>
                </form>

                <h3>Manage Products</h3>
                <div className="list">
                  {adminProducts.map((p) => (
                    <div className="list-item" key={p.id}>
                      <div>
                        <b>{p.name}</b>
                        <p>
                          {money(p.price)} · {p.category?.name}
                        </p>
                      </div>
                      {p.active !== false ? (
                        <button
                          className="outline"
                          type="button"
                          onClick={() => deactivateAdminProduct(p.id)}
                        >
                          Deactivate
                        </button>
                      ) : (
                        <span className="status off">Inactive</span>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </section>
        )}

        {/* LOGIN AND REGISTRATION */}
        {!token && tab === "auth" && (
          <section className="panel auth-panel">
            <div>
              <span className="eyebrow">JOIN THE CLUB</span>
              <h2>
                {authMode === "login" ? "Welcome back" : "Create your account"}
              </h2>
              <p>Sign in to personalize your monthly box.</p>
            </div>

            <form onSubmit={submitAuth} className="stack">
              {authMode === "register" && (
                <input
                  placeholder="Your name"
                  required
                  value={auth.name}
                  onChange={(e) => setAuth({ ...auth, name: e.target.value })}
                />
              )}
              <input
                type="email"
                placeholder="Email address"
                required
                value={auth.email}
                onChange={(e) => setAuth({ ...auth, email: e.target.value })}
              />
              <input
                type="password"
                placeholder="Password (6+ characters)"
                minLength="6"
                required
                value={auth.password}
                onChange={(e) => setAuth({ ...auth, password: e.target.value })}
              />
              <button className="primary">
                {authMode === "login" ? "Sign in" : "Create account"}
              </button>
              <button
                type="button"
                className="textbtn"
                onClick={() =>
                  setAuthMode(authMode === "login" ? "register" : "login")
                }
              >
                {authMode === "login"
                  ? "New here? Create an account"
                  : "Already have an account? Sign in"}
              </button>
            </form>
          </section>
        )}

        {/* PREFERENCE QUIZ - CUSTOMERS ONLY */}
        {isCustomer && tab === "quiz" && (
          <section className="panel">
            <span className="eyebrow">THE PREFERENCE QUIZ</span>
            <h2>Tell us what makes you happy.</h2>
            <p>We use these answers to make smarter monthly picks.</p>

            <form className="quiz-grid" onSubmit={savePref}>
              <div className="wide">
                <b>I'm interested in these categories</b>
                <p>Select as many as you like.</p>

                <div className="category-checkboxes">
                  {categories.map((c) => (
                    <label key={c.id}>
                      <input
                        type="checkbox"
                        checked={pref.categoryIds.includes(String(c.id))}
                        onChange={(e) => {
                          setPref((v) => ({
                            ...v,
                            categoryIds: e.target.checked
                              ? [...new Set([...v.categoryIds, String(c.id)])]
                              : v.categoryIds.filter(
                                  (id) => id !== String(c.id)
                                ),
                          }))

                          setPlan((v) => ({
                            ...v,
                            categoryId: String(c.id),
                          }))
                        }}
                      />
                      {c.name}
                    </label>
                  ))}
                </div>
              </div>

              <label>
                Monthly budget ($)
                <input
                  type="number"
                  min="1"
                  step=".01"
                  required
                  value={pref.monthlyBudget}
                  onChange={(e) => {
                    setPref({ ...pref, monthlyBudget: e.target.value })
                    setPlan({ ...plan, monthlyBudget: e.target.value })
                  }}
                />
              </label>

              <label className="wide">
                Things I love <small>(comma-separated keywords)</small>
                <input
                  placeholder="e.g. mystery, healthy, hydrating"
                  value={pref.interests}
                  onChange={(e) =>
                    setPref({ ...pref, interests: e.target.value })
                  }
                />
              </label>

              <label className="wide">
                Things to avoid <small>(comma-separated keywords)</small>
                <input
                  placeholder="e.g. chocolate, fragrance"
                  value={pref.avoidItems}
                  onChange={(e) =>
                    setPref({ ...pref, avoidItems: e.target.value })
                  }
                />
              </label>
              <button className="primary">Save my preferences ↗</button>
            </form>
          </section>
        )}

        {/* DISCOVER - PUBLIC AND CUSTOMER VIEW, NOT ADMIN */}
        {!isAdmin && tab === "discover" && (
          <section id="catalog" className="section">
            <div className="hero">
              <div>
                <span className="eyebrow">A LITTLE JOY, ON REPEAT</span>
                <h1>
                  Your next favourite
                  <br />
                  <em>thing is in the box.</em>
                </h1>
                <p>
                  Personalized monthly picks, made around your taste. Discover
                  books, snacks, and skincare chosen just for you.
                </p>
                <div className="hero-actions">
                  <button
                    className="primary"
                    onClick={() => setTab(isCustomer ? "quiz" : "auth")}
                  >
                    Build my box ↗
                  </button>
                  <button
                    className="textbtn"
                    onClick={() =>
                      document
                        .getElementById("catalog")
                        ?.scrollIntoView({ behavior: "smooth" })
                    }
                  >
                    Explore categories ↓
                  </button>
                </div>
              </div>
              <div className="hero-art">
                <div className="sun"></div>
                <div className="floating f1">✦ picked for you</div>
                <div className="box-illustration">
                  <div className="box-lid"></div>
                  <div className="box-front">
                    <span>
                      YOUR
                      <br />
                      MONTHLY
                      <br />
                      <b>HAPPY</b>
                    </span>
                  </div>
                  <div className="box-side"></div>
                  <div className="ribbon"></div>
                </div>
                <div className="floating f2">♡ good things inside</div>
              </div>
            </div>

            <div className="section-head">
              <div>
                <span className="eyebrow">FIND YOUR KIND OF JOY</span>
                <h2>Explore the good stuff.</h2>
              </div>
              <span className="muted">Curated for curious people</span>
            </div>

            <div className="category-grid">
              {categories.map((c, i) => (
                <button
                  key={c.id}
                  className={`category-card c${i % 3}`}
                  onClick={() => {
                    setPref((v) => ({
                      ...v,
                      categoryIds: [
                        ...new Set([...v.categoryIds, String(c.id)]),
                      ],
                    }))

                    setPlan((v) => ({
                      ...v,
                      categoryId: String(c.id),
                    }))

                    setTab(isCustomer ? "quiz" : "auth")
                  }}
                >
                  <span className="category-emoji">
                    {["📚", "🍓", "🌿", "🎁"][i % 4]}
                  </span>
                  <span>
                    <b>{c.name}</b>
                    <small>{c.description}</small>
                  </span>
                  <span className="arrow">↗</span>
                </button>
              ))}
            </div>

            <div className="section-head product-heading">
              <div>
                <span className="eyebrow">A FEW CROWD FAVOURITES</span>
                <h2>Meet the possibilities.</h2>
              </div>
            </div>

            <div className="product-grid">
              {isCustomer && recommendations.length === 0 && (
                <p>
                  No matching products found. Try increasing your monthly budget
                  or updating your preferences.
                </p>
              )}

              {(isCustomer ? recommendations : products).map((item, i) => {
                const p = item.product || item

                return (
                  <article className="product-card" key={p.id}>
                    <div className={`product-art art${i % 4}`}>
                      {["✧", "❋", "✿", "✦"][i % 4]}
                    </div>
                    <div className="product-info">
                      <small>{p.category?.name}</small>
                      <h3>{p.name}</h3>
                      <p>{p.description}</p>
                      <div className="product-bottom">
                        <b>{money(p.price)}</b>
                        <button
                          onClick={() => {
                            setRate({ ...rate, productId: String(p.id) })
                            setTab(isCustomer ? "ratings" : "auth")
                          }}
                        >
                          Rate item ↗
                        </button>
                      </div>
                    </div>
                  </article>
                )
              })}
            </div>
          </section>
        )}

        {/* CUSTOMER SUBSCRIPTIONS */}
        {isCustomer && tab === "subscriptions" && (
          <section className="panel">
            <span className="eyebrow">YOUR MONTHLY RITUAL</span>
            <h2>Subscriptions</h2>

            <form className="subscribe-form" onSubmit={createPlan}>
              <label>
                Box name
                <input
                  value={plan.planName}
                  required
                  onChange={(e) =>
                    setPlan({ ...plan, planName: e.target.value })
                  }
                />
              </label>
              <label>
                Category
                <select
                  required
                  value={plan.categoryId}
                  onChange={(e) =>
                    setPlan({ ...plan, categoryId: e.target.value })
                  }
                >
                  <option value="">Choose category</option>
                  {categories.map((c) => (
                    <option value={c.id} key={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Monthly budget ($)
                <input
                  type="number"
                  min="1"
                  step=".01"
                  required
                  value={plan.monthlyBudget}
                  onChange={(e) =>
                    setPlan({ ...plan, monthlyBudget: e.target.value })
                  }
                />
              </label>
              <button className="primary">Start my subscription ↗</button>
            </form>

            <div className="list">
              {subs.map((s) => (
                <div className="list-item" key={s.id}>
                  <div>
                    <b>{s.planName}</b>
                    <p>
                      {s.category.name} · {money(s.monthlyBudget)} / month ·
                      Next box: {s.nextOrderDate}
                    </p>
                  </div>
                  <span className={s.active ? "status" : "status off"}>
                    {s.active ? "Active" : "Cancelled"}
                  </span>
                  {s.active && (
                    <button className="outline" onClick={() => cancel(s.id)}>
                      Cancel
                    </button>
                  )}
                </div>
              ))}
            </div>
          </section>
        )}

        {/* CUSTOMER ORDERS */}
        {isCustomer && tab === "orders" && (
          <section className="panel">
            <span className="eyebrow">A HISTORY OF HAPPY MAIL</span>
            <h2>My monthly boxes</h2>

            {orders.length ? (
              <div className="order-grid">
                {orders.map((o) => (
                  <article className="order-card" key={o.id}>
                    <div className="order-top">
                      <span>BOX #{o.id}</span>
                      <span className="status">{o.status}</span>
                    </div>
                    <h3>{o.subscriptionPlan.planName}</h3>
                    <p>
                      {o.orderDate} · {o.products.length} items
                    </p>
                    <div className="order-products">
                      {o.products.map((p) => (
                        <span key={p.id}>{p.name}</span>
                      ))}
                    </div>
                    <b className="order-total">{money(o.total)}</b>
                  </article>
                ))}
              </div>
            ) : (
              <p>
                Your first box will appear here when you start a subscription.
              </p>
            )}
          </section>
        )}

        {/* CUSTOMER RATINGS */}
        {isCustomer && tab === "ratings" && (
          <section className="panel">
            <span className="eyebrow">HELP US LEARN YOUR TASTE</span>
            <h2>Rate your discoveries.</h2>

            <form className="rating-form" onSubmit={submitRating}>
              <label>
                Product
                <select
                  required
                  value={rate.productId}
                  onChange={(e) =>
                    setRate({ ...rate, productId: e.target.value })
                  }
                >
                  <option value="">Choose a product</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Your rating
                <select
                  value={rate.score}
                  onChange={(e) => setRate({ ...rate, score: e.target.value })}
                >
                  {[5, 4, 3, 2, 1].map((n) => (
                    <option value={n} key={n}>
                      {"★".repeat(n)} ({n}/5)
                    </option>
                  ))}
                </select>
              </label>
              <label className="wide">
                Review (optional)
                <input
                  value={rate.comment}
                  onChange={(e) =>
                    setRate({ ...rate, comment: e.target.value })
                  }
                  placeholder="What did you think?"
                />
              </label>
              <button className="primary">Save rating ↗</button>
            </form>

            <h3 className="subheading">Your rating history</h3>
            {ratings.map((r) => (
              <div className="list-item" key={r.id}>
                <div>
                  <b>{r.product.name}</b>
                  <p>{r.comment || "No written review"}</p>
                </div>
                <span className="stars">
                  {"★".repeat(r.score)}
                  {"☆".repeat(5 - r.score)}
                </span>
              </div>
            ))}
          </section>
        )}

        {message && (
          <div className="notice" onClick={() => setMessage("")}>
            {message}
            <span> ×</span>
          </div>
        )}

        <footer>
          <div className="brand">
            <span className="logo">B</span>
            <div>
              <b>boxly</b>
              <small>GOOD THINGS, DELIVERED.</small>
            </div>
          </div>
          <span>Made around your taste. One box at a time.</span>
          <span>© 2026 Boxly</span>
        </footer>
      </main>
    </div>
  )
}
