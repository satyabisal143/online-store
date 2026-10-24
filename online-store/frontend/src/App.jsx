import { useEffect, useState } from 'react'
import { getOrders, getProducts, placeOrder } from './api.js'

const money = (n) => `$${Number(n).toFixed(2)}`

export default function App() {
  const [view, setView] = useState('products')
  const [products, setProducts] = useState([])
  const [orders, setOrders] = useState([])
  const [cart, setCart] = useState({}) // { productId: quantity }
  const [message, setMessage] = useState(null) // { type: 'ok' | 'error', text }

  const loadProducts = () =>
    getProducts().then(setProducts).catch((e) => setMessage({ type: 'error', text: e.message }))
  const loadOrders = () =>
    getOrders().then(setOrders).catch((e) => setMessage({ type: 'error', text: e.message }))

  useEffect(() => {
    loadProducts()
  }, [])

  useEffect(() => {
    if (view === 'orders') loadOrders()
  }, [view])

  const addToCart = (id) => setCart((c) => ({ ...c, [id]: (c[id] || 0) + 1 }))
  const removeFromCart = (id) =>
    setCart((c) => {
      const { [id]: _removed, ...rest } = c
      return rest
    })

  const cartItems = Object.entries(cart).map(([id, quantity]) => ({
    product: products.find((p) => p.id === Number(id)),
    quantity,
  })).filter((i) => i.product)
  const cartTotal = cartItems.reduce((sum, i) => sum + i.product.price * i.quantity, 0)

  const checkout = async () => {
    try {
      const order = await placeOrder(
        cartItems.map((i) => ({ productId: i.product.id, quantity: i.quantity })),
      )
      setCart({})
      setMessage({ type: 'ok', text: `Order #${order.id} placed - total ${money(order.total)}` })
    } catch (e) {
      setMessage({ type: 'error', text: e.message })
    }
  }

  return (
    <div className="app">
      <header>
        <h1>Online Store</h1>
        <nav>
          <button className={view === 'products' ? 'active' : ''} onClick={() => setView('products')}>
            Shop
          </button>
          <button className={view === 'orders' ? 'active' : ''} onClick={() => setView('orders')}>
            Orders
          </button>
        </nav>
      </header>

      {message && (
        <div className={`message ${message.type}`} onClick={() => setMessage(null)}>
          {message.text}
        </div>
      )}

      {view === 'products' && (
        <div className="layout">
          <section className="grid">
            {products.map((p) => (
              <div className="card" key={p.id}>
                <h3>{p.name}</h3>
                <p className="price">{money(p.price)}</p>
                <p className="stock">{p.stock} in stock</p>
                <button onClick={() => addToCart(p.id)} disabled={p.stock === 0}>
                  Add to cart
                </button>
              </div>
            ))}
          </section>

          <aside className="cart">
            <h2>Cart</h2>
            {cartItems.length === 0 && <p>Your cart is empty.</p>}
            {cartItems.map(({ product, quantity }) => (
              <div className="cart-row" key={product.id}>
                <span>
                  {product.name} x {quantity}
                </span>
                <span>
                  {money(product.price * quantity)}{' '}
                  <button className="link" onClick={() => removeFromCart(product.id)}>
                    remove
                  </button>
                </span>
              </div>
            ))}
            {cartItems.length > 0 && (
              <>
                <p className="total">Total: {money(cartTotal)}</p>
                <button onClick={checkout}>Place order</button>
              </>
            )}
          </aside>
        </div>
      )}

      {view === 'orders' && (
        <section>
          {orders.length === 0 && <p>No orders yet.</p>}
          {orders.map((o) => (
            <div className="card order" key={o.id}>
              <h3>
                Order #{o.id} - {money(o.total)}
              </h3>
              <small>{new Date(o.createdAt).toLocaleString()}</small>
              <ul>
                {o.lines.map((l, i) => (
                  <li key={i}>
                    {l.productName} x {l.quantity} @ {money(l.unitPrice)}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </section>
      )}
    </div>
  )
}
