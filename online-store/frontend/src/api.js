// All requests go through the API gateway - the frontend never talks to individual services.
const BASE_URL = 'http://localhost:8080/api'

async function request(path, options) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!response.ok) {
    // Spring returns RFC 7807 "problem detail" JSON with a `detail` message.
    const problem = await response.json().catch(() => ({}))
    throw new Error(problem.detail || `Request failed (${response.status})`)
  }
  return response.json()
}

export const getProducts = () => request('/products')
export const getOrders = () => request('/orders')
export const placeOrder = (items) =>
  request('/orders', { method: 'POST', body: JSON.stringify({ items }) })
