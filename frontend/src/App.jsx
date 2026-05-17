import {
  Boxes,
  Building2,
  CheckCircle2,
  ClipboardList,
  Eye,
  EyeOff,
  Factory,
  History,
  LayoutGrid,
  List,
  LogOut,
  PackagePlus,
  PackageSearch,
  Pencil,
  Plus,
  QrCode,
  RefreshCw,
  ScanBarcode,
  ShieldCheck,
  ShoppingCart,
  Tags,
  Truck,
  UserCog,
  UserPlus,
  Warehouse as WarehouseIcon
} from 'lucide-react';
import { useEffect, useMemo, useState, memo, useCallback } from 'react';
import { API_URL, apiBlob, apiRequest } from './api.js';

const tabs = [
  { id: 'dashboard', label: 'Dashboard', icon: PackageSearch },
  { id: 'products', label: 'Catalog', icon: QrCode },
  { id: 'warehouse', label: 'Warehouse', icon: Building2 },
  { id: 'receiving', label: 'Receiving', icon: ScanBarcode },
  { id: 'orders', label: 'Orders', icon: ClipboardList },
  { id: 'procurement', label: 'Procurement', icon: ShoppingCart },
  { id: 'users', label: 'Users', icon: UserCog }
];

const blankProduct = {
  sku: '',
  name: '',
  barcode: '',
  description: '',
  unitVolume: 1,
  reorderThreshold: 10,
  price: 0,
  weight: 0,
  categoryId: '',
  warehouseId: ''
};

const blankUser = {
  username: '',
  password: '',
  role: 'OPERATOR',
  name: '',
  email: '',
  contactNumber: '',
  status: 'ACTIVE',
  warehouseId: ''
};

function App() {
  const [auth, setAuth] = useState(() => {
    const stored = localStorage.getItem('wms-auth');
    return stored ? JSON.parse(stored) : null;
  });
  const [activeTab, setActiveTab] = useState('dashboard');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [zones, setZones] = useState([]);
  const [aisles, setAisles] = useState([]);
  const [bins, setBins] = useState([]);
  const [orders, setOrders] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [users, setUsers] = useState([]);
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [productForm, setProductForm] = useState(blankProduct);
  const [categoryForm, setCategoryForm] = useState({ name: '', warehouseId: '', parentCategoryId: '', preferredZoneId: '' });
  const [zoneForm, setZoneForm] = useState({ warehouseId: '', code: '', name: '' });
  const [aisleForm, setAisleForm] = useState({ zoneId: '', code: '' });
  const [binForm, setBinForm] = useState({ aisleId: '', code: '', capacity: 100 });
  const [receiveForm, setReceiveForm] = useState({ productId: '', quantity: 1, reference: '', batchNumber: '', expiryDate: '' });
  const [adjustForm, setAdjustForm] = useState({ productId: '', binId: '', quantityDelta: 0, reason: '' });
  const [transferForm, setTransferForm] = useState({ productId: '', fromBinId: '', toBinId: '', quantity: 1, reference: '' });
  const [orderForm, setOrderForm] = useState({ warehouseId: '', expectedShipDate: '', productId: '', quantity: 1 });
  const [supplierForm, setSupplierForm] = useState({ name: '', address: '', contactEmail: '', phone: '' });
  const [purchaseForm, setPurchaseForm] = useState({ supplierId: '', warehouseId: '', expectedDate: '', productId: '', quantity: 1 });
  const [userForm, setUserForm] = useState(blankUser);
  const [editingUserId, setEditingUserId] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);
  const [authMode, setAuthMode] = useState('login');
  const [registerForm, setRegisterForm] = useState({ username: '', password: '', name: '', email: '', contactNumber: '', warehouseCode: '', warehouseName: '', warehouseAddress: '' });

  const token = auth?.token;
  const isAdmin = auth?.roles?.includes('ADMIN');
  const visibleTabs = useMemo(() => (isAdmin ? tabs : tabs.filter((tab) => tab.id !== 'users')), [isAdmin]);

  const totals = useMemo(() => {
    const units = inventory.reduce((sum, row) => sum + row.quantity, 0);
    const available = inventory.reduce((sum, row) => sum + row.availableQuantity, 0);
    const capacity = bins.reduce((sum, bin) => sum + bin.capacity, 0);
    const used = bins.reduce((sum, bin) => sum + bin.usedCapacity, 0);
    return {
      skus: products.length,
      categories: categories.length,
      suppliers: suppliers.length,
      openPurchaseOrders: purchaseOrders.filter((po) => po.status !== 'RECEIVED' && po.status !== 'CANCELLED').length,
      units,
      available,
      bins: bins.length,
      usedPercent: capacity === 0 ? 0 : Math.round((used / capacity) * 100)
    };
  }, [bins, categories, inventory, products, purchaseOrders, suppliers]);

  useEffect(() => {
    if (token) {
      loadAll();
    }
  }, [token]);

  async function run(action, success) {
    setLoading(true);
    setMessage('');
    try {
      await action();
      if (success) {
        setMessage(success);
      }
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadAll() {
    await run(async () => {
      const [
        nextProducts,
        nextCategories,
        nextInventory,
        nextWarehouses,
        nextZones,
        nextAisles,
        nextBins,
        nextOrders,
        nextSuppliers,
        nextPurchaseOrders,
        nextUsers,
        nextAuditLogs
      ] = await Promise.all([
        apiRequest('/products', { token }),
        apiRequest('/product-categories', { token }),
        apiRequest('/inventory', { token }),
        apiRequest('/warehouses', { token }),
        apiRequest('/zones', { token }),
        apiRequest('/aisles', { token }),
        apiRequest('/bins', { token }),
        apiRequest('/orders', { token }),
        apiRequest('/procurement/suppliers', { token }),
        apiRequest('/procurement/purchase-orders', { token }),
        isAdmin ? apiRequest('/users', { token }) : Promise.resolve([]),
        apiRequest('/audit-logs?limit=50', { token }).catch(() => [])
      ]);
      setProducts(nextProducts);
      setCategories(nextCategories);
      setInventory(nextInventory);
      setWarehouses(nextWarehouses);
      setZones(nextZones);
      setAisles(nextAisles);
      setBins(nextBins);
      setOrders(nextOrders);
      setSuppliers(nextSuppliers);
      setPurchaseOrders(nextPurchaseOrders);
      setUsers(nextUsers);
      setAuditLogs(nextAuditLogs);
    });
  }

  async function login(event) {
    event.preventDefault();
    await run(async () => {
      const nextAuth = await apiRequest('/auth/login', {
        method: 'POST',
        body: loginForm
      });
      localStorage.setItem('wms-auth', JSON.stringify(nextAuth));
      setAuth(nextAuth);
    }, 'Signed in.');
  }

  function logout() {
    localStorage.removeItem('wms-auth');
    setAuth(null);
  }

  async function register(event) {
    event.preventDefault();
    await run(async () => {
      const nextAuth = await apiRequest('/auth/register', {
        method: 'POST',
        body: registerForm
      });
      localStorage.setItem('wms-auth', JSON.stringify(nextAuth));
      setAuth(nextAuth);
      setRegisterForm({ username: '', password: '', name: '', email: '', contactNumber: '', warehouseCode: '', warehouseName: '', warehouseAddress: '' });
    }, 'Account created successfully!');
  }

  async function createProduct(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/products', {
        method: 'POST',
        token,
        body: normalizeIds(productForm, ['categoryId', 'warehouseId'])
      });
      setProductForm(blankProduct);
      await loadAll();
    }, 'Product created.');
  }

  async function createCategory(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/product-categories', {
        method: 'POST',
        token,
        body: normalizeIds(categoryForm, ['warehouseId', 'parentCategoryId', 'preferredZoneId'])
      });
      setCategoryForm({ name: '', warehouseId: '', parentCategoryId: '', preferredZoneId: '' });
      await loadAll();
    }, 'Category created.');
  }

  async function createZone(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/zones', { method: 'POST', token, body: normalizeIds(zoneForm, ['warehouseId']) });
      setZoneForm({ warehouseId: '', code: '', name: '' });
      await loadAll();
    }, 'Zone created.');
  }

  async function createAisle(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/aisles', { method: 'POST', token, body: normalizeIds(aisleForm, ['zoneId']) });
      setAisleForm({ zoneId: '', code: '' });
      await loadAll();
    }, 'Aisle created.');
  }

  async function createBin(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/bins', { method: 'POST', token, body: normalizeIds(binForm, ['aisleId']) });
      setBinForm({ aisleId: '', code: '', capacity: 100 });
      await loadAll();
    }, 'Bin created.');
  }

  async function receiveStock(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/inventory/receive', {
        method: 'POST',
        token,
        body: normalizeIds(cleanDates(receiveForm, ['expiryDate']), ['productId'])
      });
      setReceiveForm({ productId: '', quantity: 1, reference: '', batchNumber: '', expiryDate: '' });
      await loadAll();
    }, 'Stock received and put away.');
  }

  async function adjustStock(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/inventory/adjust', {
        method: 'POST',
        token,
        body: normalizeIds(adjustForm, ['productId', 'binId'])
      });
      setAdjustForm({ productId: '', binId: '', quantityDelta: 0, reason: '' });
      await loadAll();
    }, 'Stock adjustment recorded.');
  }

  async function transferStock(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/inventory/transfer', {
        method: 'POST',
        token,
        body: normalizeIds(transferForm, ['productId', 'fromBinId', 'toBinId'])
      });
      setTransferForm({ productId: '', fromBinId: '', toBinId: '', quantity: 1, reference: '' });
      await loadAll();
    }, 'Stock transferred between bins.');
  }

  async function createOrder(event) {
    event.preventDefault();
    await run(async () => {
      const payload = normalizeIds(cleanDates(orderForm, ['expectedShipDate']), ['warehouseId', 'productId']);
      await apiRequest('/orders', {
        method: 'POST',
        token,
        body: {
          warehouseId: payload.warehouseId,
          expectedShipDate: payload.expectedShipDate,
          lines: [{ productId: payload.productId, quantity: payload.quantity }]
        }
      });
      setOrderForm({ warehouseId: '', expectedShipDate: '', productId: '', quantity: 1 });
      await loadAll();
    }, 'Order created.');
  }

  async function createSupplier(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/procurement/suppliers', { method: 'POST', token, body: supplierForm });
      setSupplierForm({ name: '', address: '', contactEmail: '', phone: '' });
      await loadAll();
    }, 'Supplier created.');
  }

  async function createPurchaseOrder(event) {
    event.preventDefault();
    await run(async () => {
      const payload = normalizeIds(cleanDates(purchaseForm, ['expectedDate']), ['supplierId', 'warehouseId', 'productId']);
      await apiRequest('/procurement/purchase-orders', {
        method: 'POST',
        token,
        body: {
          supplierId: payload.supplierId,
          warehouseId: payload.warehouseId,
          expectedDate: payload.expectedDate,
          items: [{ productId: payload.productId, quantity: payload.quantity }]
        }
      });
      setPurchaseForm({ supplierId: '', warehouseId: '', expectedDate: '', productId: '', quantity: 1 });
      await loadAll();
    }, 'Purchase order created.');
  }

  async function saveUser(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest(editingUserId ? `/users/${editingUserId}` : '/users', {
        method: editingUserId ? 'PUT' : 'POST',
        token,
        body: normalizeIds(userForm, ['warehouseId'])
      });
      setUserForm(blankUser);
      setEditingUserId(null);
      await loadAll();
    }, editingUserId ? 'User updated.' : 'User created.');
  }

  function editUser(user) {
    setEditingUserId(user.id);
    setUserForm({
      username: user.username,
      password: '',
      role: user.role,
      name: user.name || '',
      email: user.email || '',
      contactNumber: user.contactNumber || '',
      status: user.status || 'ACTIVE',
      warehouseId: user.warehouseId || ''
    });
    setActiveTab('users');
  }

  function cancelUserEdit() {
    setEditingUserId(null);
    setUserForm(blankUser);
  }

  async function transitionOrder(id, action, label) {
    await run(async () => {
      await apiRequest(`/orders/${id}/${action}`, { method: 'POST', token });
      await loadAll();
    }, label);
  }

  if (!auth) {
    return (
      <main className="auth-shell">
        <div className="auth-container">
          <div className="auth-hero">
            <div className="hero-icon"><ShieldCheck size={32} /></div>
            <h1>Infotact WMS</h1>
            <p>Enterprise-grade warehouse management with real-time inventory tracking, smart order fulfillment, and audit compliance.</p>
            <div className="hero-features">
              <div className="hero-feature"><CheckCircle2 size={18} />Real-time inventory tracking</div>
              <div className="hero-feature"><CheckCircle2 size={18} />Multi-warehouse support</div>
              <div className="hero-feature"><CheckCircle2 size={18} />Role-based access control</div>
              <div className="hero-feature"><CheckCircle2 size={18} />Complete audit trail</div>
            </div>
          </div>
          <div className="auth-panel">
            {authMode === 'login' ? (
              <>
                <div className="auth-header">
                  <h2>Welcome Back</h2>
                  <p>Sign in to your warehouse console</p>
                </div>
                <form onSubmit={login} className="auth-form">
                  <div className="field-group">
                    <label>Username</label>
                    <input id="login-username" autoComplete="username" required placeholder="Enter username" value={loginForm.username} onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })} />
                  </div>
                  <div className="field-group">
                    <label>Password</label>
                    <input id="login-password" autoComplete="current-password" required type="password" placeholder="Enter password" value={loginForm.password} onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} />
                  </div>
                  <button type="submit" disabled={loading}><ShieldCheck size={18} />{loading ? 'Signing in...' : 'Sign In'}</button>
                </form>
                {message && <div className={`auth-message ${message.includes('Signed') ? 'success' : 'error'}`}>{message}</div>}
                <div className="auth-switch">
                  Don&apos;t have an account? <button type="button" onClick={() => { setAuthMode('register'); setMessage(''); }}>Create Admin Account</button>
                </div>
              </>
            ) : (
              <>
                <div className="auth-header">
                  <h2>Create Admin Account</h2>
                  <p>Register with your warehouse details</p>
                </div>
                <form onSubmit={register} className="auth-form">
                  <div className="field-row">
                    <div className="field-group">
                      <label>Full Name</label>
                      <input id="reg-name" required placeholder="Your full name" value={registerForm.name} onChange={(e) => setRegisterForm({ ...registerForm, name: e.target.value })} />
                    </div>
                    <div className="field-group">
                      <label>Username</label>
                      <input id="reg-username" required minLength={3} pattern="^[A-Za-z0-9][A-Za-z0-9._-]*$" placeholder="Choose a username" value={registerForm.username} onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })} />
                      <span className="field-hint">{registerForm.username && (registerForm.username.length < 3 ? <span className="error">Min 3 characters</span> : <span className="success">✓</span>)}</span>
                    </div>
                  </div>
                  <div className="field-group">
                    <label>Email</label>
                    <input id="reg-email" type="email" required placeholder="admin@example.com" value={registerForm.email} onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })} />
                    <span className="field-hint">{registerForm.email && (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email) ? <span className="success">✓ Valid email</span> : <span className="error">Enter a valid email</span>)}</span>
                  </div>
                  <div className="field-row">
                    <div className="field-group">
                      <label>Password</label>
                      <input id="reg-password" type="password" required minLength={6} placeholder="Min 6 characters" value={registerForm.password} onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })} />
                      <div className="password-strength">
                        {[1,2,3,4].map(i => {
                          const len = registerForm.password.length;
                          const strength = len === 0 ? 0 : len < 6 ? 1 : len < 8 ? 2 : len < 12 ? 3 : 4;
                          const cls = strength >= i ? (strength <= 1 ? 'weak' : strength <= 2 ? 'medium' : 'strong') : '';
                          return <div key={i} className={`bar ${strength >= i ? 'active' : ''} ${cls}`} />;
                        })}
                      </div>
                    </div>
                    <div className="field-group">
                      <label>Phone (optional)</label>
                      <input id="reg-phone" placeholder="+91 90000 00000" value={registerForm.contactNumber} onChange={(e) => setRegisterForm({ ...registerForm, contactNumber: e.target.value })} />
                    </div>
                  </div>
                  <div className="auth-divider">Your Warehouse</div>
                  <div className="field-row">
                    <div className="field-group">
                      <label>Warehouse Code</label>
                      <input id="reg-wh-code" required pattern="^[A-Za-z0-9._-]+$" minLength={2} placeholder="e.g. BLR-01" value={registerForm.warehouseCode} onChange={(e) => setRegisterForm({ ...registerForm, warehouseCode: e.target.value })} />
                    </div>
                    <div className="field-group">
                      <label>Warehouse Name</label>
                      <input id="reg-wh-name" required placeholder="e.g. Bengaluru Hub" value={registerForm.warehouseName} onChange={(e) => setRegisterForm({ ...registerForm, warehouseName: e.target.value })} />
                    </div>
                  </div>
                  <div className="field-group">
                    <label>Warehouse Address (optional)</label>
                    <input id="reg-wh-address" placeholder="Full address" value={registerForm.warehouseAddress} onChange={(e) => setRegisterForm({ ...registerForm, warehouseAddress: e.target.value })} />
                  </div>
                  <button type="submit" disabled={loading}><UserPlus size={18} />{loading ? 'Creating...' : 'Create Account & Warehouse'}</button>
                </form>
                {message && <div className={`auth-message ${message.includes('success') ? 'success' : 'error'}`}>{message}</div>}
                <div className="auth-switch">
                  Already have an account? <button type="button" onClick={() => { setAuthMode('login'); setMessage(''); }}>Sign In</button>
                </div>
              </>
            )}
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand-line compact">
          <Boxes size={28} />
          <div>
            <p>Infotact</p>
            <h1>WMS Console</h1>
          </div>
        </div>
        <nav className="tabs">
          {visibleTabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button key={tab.id} className={activeTab === tab.id ? 'active' : ''} onClick={() => setActiveTab(tab.id)}>
                <Icon size={18} />
                {tab.label}
              </button>
            );
          })}
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">{auth.roles.join(', ')} &bull; {auth.warehouseName || 'Global'}</p>
            <h2>{visibleTabs.find((tab) => tab.id === activeTab)?.label}</h2>
          </div>
          <div className="topbar-actions">
            {message && <p className="message">{message}</p>}
            <button onClick={loadAll} disabled={loading} title="Refresh data">
              <RefreshCw size={18} />
            </button>
            <button onClick={logout} title="Sign out">
              <LogOut size={18} />
            </button>
          </div>
        </header>

        {activeTab === 'dashboard' && <Dashboard totals={totals} inventory={inventory} bins={bins} orders={orders} purchaseOrders={purchaseOrders} auditLogs={auditLogs} />}
        {activeTab === 'products' && (
          <Catalog
            isAdmin={isAdmin}
            token={token}
            products={products}
            categories={categories}
            warehouses={warehouses}
            zones={zones}
            productForm={productForm}
            setProductForm={setProductForm}
            categoryForm={categoryForm}
            setCategoryForm={setCategoryForm}
            createProduct={createProduct}
            createCategory={createCategory}
          />
        )}
        {activeTab === 'warehouse' && (
          <Warehouse
            isAdmin={isAdmin}
            warehouses={warehouses}
            zones={zones}
            aisles={aisles}
            bins={bins}
            zoneForm={zoneForm}
            setZoneForm={setZoneForm}
            aisleForm={aisleForm}
            setAisleForm={setAisleForm}
            binForm={binForm}
            setBinForm={setBinForm}
            createZone={createZone}
            createAisle={createAisle}
            createBin={createBin}
          />
        )}
        {activeTab === 'receiving' && (
          <Receiving
            isAdmin={isAdmin}
            products={products}
            bins={bins}
            inventory={inventory}
            receiveForm={receiveForm}
            setReceiveForm={setReceiveForm}
            adjustForm={adjustForm}
            setAdjustForm={setAdjustForm}
            transferForm={transferForm}
            setTransferForm={setTransferForm}
            receiveStock={receiveStock}
            adjustStock={adjustStock}
            transferStock={transferStock}
          />
        )}
        {activeTab === 'orders' && (
          <Orders
            warehouses={warehouses}
            products={products}
            orders={orders}
            orderForm={orderForm}
            setOrderForm={setOrderForm}
            createOrder={createOrder}
            transitionOrder={transitionOrder}
          />
        )}
        {activeTab === 'procurement' && (
          <Procurement
            isAdmin={isAdmin}
            warehouses={warehouses}
            products={products}
            suppliers={suppliers}
            purchaseOrders={purchaseOrders}
            supplierForm={supplierForm}
            setSupplierForm={setSupplierForm}
            purchaseForm={purchaseForm}
            setPurchaseForm={setPurchaseForm}
            createSupplier={createSupplier}
            createPurchaseOrder={createPurchaseOrder}
          />
        )}
        {activeTab === 'users' && isAdmin && (
          <Users
            users={users}
            warehouses={warehouses}
            userForm={userForm}
            setUserForm={setUserForm}
            editingUserId={editingUserId}
            saveUser={saveUser}
            editUser={editUser}
            cancelUserEdit={cancelUserEdit}
          />
        )}
      </section>
    </main>
  );
}

const Dashboard = memo(function Dashboard({ totals, inventory, bins, orders, purchaseOrders, auditLogs }) {
  const recentOrders = orders.slice(0, 5);
  const recentPurchases = purchaseOrders.slice(0, 4);
  return (
    <div className="view-grid">
      <section className="metric-strip">
        <Metric icon={PackageSearch} label="Active SKUs" value={totals.skus} />
        <Metric icon={Tags} label="Categories" value={totals.categories} />
        <Metric icon={Boxes} label="Available Units" value={totals.available} />
        <Metric icon={Truck} label="Capacity Used" value={`${totals.usedPercent}%`} />
      </section>
      <section className="panel wide">
        <PanelTitle icon={History} title="Audit History &amp; Logs" />
        {auditLogs.length === 0 ? (
          <div className="empty-state">
            <History size={40} />
            <p>No audit logs yet. Receive stock or fulfill orders to generate transaction history.</p>
          </div>
        ) : (
          <table className="audit-table">
            <thead>
              <tr>
                <th>Type</th>
                <th>Product</th>
                <th>SKU</th>
                <th>Bin</th>
                <th>Qty Change</th>
                <th>Reference</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.map((log) => {
                const typeCls = log.type.toLowerCase().includes('receive') ? 'receive' : log.type.toLowerCase().includes('pick') ? 'pick' : log.type.toLowerCase().includes('release') ? 'release' : 'adjust';
                return (
                  <tr key={log.id}>
                    <td><span className={`audit-type ${typeCls}`}>{log.type}</span></td>
                    <td>{log.productName}</td>
                    <td>{log.productSku}</td>
                    <td>{log.binCode}</td>
                    <td><span className={`audit-delta ${log.quantityDelta >= 0 ? 'positive' : 'negative'}`}>{log.quantityDelta >= 0 ? '+' : ''}{log.quantityDelta}</span></td>
                    <td>{log.reference || '—'}</td>
                    <td className="audit-time">{new Date(log.createdAt).toLocaleString()}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </section>
      <section className="panel wide">
        <PanelTitle icon={PackageSearch} title="Inventory Ledger" />
        <InventoryTable inventory={inventory} />
      </section>
      <section className="panel">
        <PanelTitle icon={Building2} title="Bin Utilization" />
        <div className="stack">
          {bins.map((bin) => (
            <div className="util-row" key={bin.id}>
              <span>{bin.code}</span>
              <meter min="0" max={bin.capacity} value={bin.usedCapacity} />
              <strong>{bin.usedCapacity}/{bin.capacity}</strong>
            </div>
          ))}
        </div>
      </section>
      <section className="panel">
        <PanelTitle icon={ClipboardList} title="Recent Orders" />
        <div className="stack">
          {recentOrders.map((order) => (
            <div className="order-chip" key={order.id}>
              <span>{order.orderNumber}</span>
              <strong>{order.status}</strong>
            </div>
          ))}
          {recentPurchases.map((po) => (
            <div className="order-chip" key={`po-${po.id}`}>
              <span>PO-{po.id} {po.supplierName}</span>
              <strong>{po.status}</strong>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
});

const Catalog = memo(function Catalog(props) {
  const {
    isAdmin,
    token,
    products,
    categories,
    warehouses,
    zones,
    productForm,
    setProductForm,
    categoryForm,
    setCategoryForm,
    createProduct,
    createCategory
  } = props;

  const [barcodeUrls, setBarcodeUrls] = useState({});
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [viewMode, setViewMode] = useState('grid');
  const [activeFormTab, setActiveFormTab] = useState('product');

  useEffect(() => {
    if (!token || products.length === 0) {
      setBarcodeUrls({});
      return undefined;
    }
    let cancelled = false;
    const nextUrls = {};

    Promise.all(products.map(async (product) => {
      const blob = await apiBlob(`/products/${product.id}/barcode`, { token });
      nextUrls[product.id] = URL.createObjectURL(blob);
    })).then(() => {
      if (!cancelled) {
        setBarcodeUrls(nextUrls);
      }
    }).catch(() => {
      if (!cancelled) {
        setBarcodeUrls({});
      }
    });

    return () => {
      cancelled = true;
      Object.values(nextUrls).forEach((url) => URL.revokeObjectURL(url));
    };
  }, [products, token]);

  const filteredProducts = products.filter((product) => {
    const matchesSearch = 
      product.sku.toLowerCase().includes(searchTerm.toLowerCase()) || 
      product.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = 
      selectedCategory === 'ALL' || 
      String(product.categoryId) === String(selectedCategory);
    return matchesSearch && matchesCategory;
  });

  return (
    <div className="split-view">
      {isAdmin && (
        <section className="panel" style={{ flex: '0 0 350px' }}>
          <div className="form-toggle-tabs" style={{ display: 'flex', gap: '8px', marginBottom: '20px', borderBottom: '1px solid var(--border-light)', paddingBottom: '10px' }}>
            <button 
              type="button" 
              className={`tab-btn ${activeFormTab === 'product' ? 'active' : ''}`}
              onClick={() => setActiveFormTab('product')}
              style={{ flex: 1, padding: '8px', borderRadius: '8px', border: 'none', background: activeFormTab === 'product' ? 'var(--primary-color)' : 'transparent', color: activeFormTab === 'product' ? 'white' : 'var(--text-secondary)', fontWeight: 600, cursor: 'pointer', transition: 'all 0.2s' }}
            >
              <PackagePlus size={16} style={{ marginRight: '6px', verticalAlign: 'middle' }} />
              Product
            </button>
            <button 
              type="button" 
              className={`tab-btn ${activeFormTab === 'category' ? 'active' : ''}`}
              onClick={() => setActiveFormTab('category')}
              style={{ flex: 1, padding: '8px', borderRadius: '8px', border: 'none', background: activeFormTab === 'category' ? 'var(--primary-color)' : 'transparent', color: activeFormTab === 'category' ? 'white' : 'var(--text-secondary)', fontWeight: 600, cursor: 'pointer', transition: 'all 0.2s' }}
            >
              <Tags size={16} style={{ marginRight: '6px', verticalAlign: 'middle' }} />
              Category
            </button>
          </div>

          {activeFormTab === 'product' ? (
            <>
              <PanelTitle icon={PackagePlus} title="Create Product" />
              <form onSubmit={createProduct} className="form-grid" style={{ gap: '10px' }}>
                <input required pattern="^[A-Za-z0-9][A-Za-z0-9._-]*$" maxLength="80" placeholder="SKU" value={productForm.sku} onChange={(event) => setProductForm({ ...productForm, sku: event.target.value })} />
                <input required maxLength="160" placeholder="Name" value={productForm.name} onChange={(event) => setProductForm({ ...productForm, name: event.target.value })} />
                <input pattern="^[A-Za-z0-9._-]*$" maxLength="120" placeholder="Barcode (Optional)" value={productForm.barcode} onChange={(event) => setProductForm({ ...productForm, barcode: event.target.value })} />
                <Select required value={productForm.warehouseId} onChange={(warehouseId) => setProductForm({ ...productForm, warehouseId })} label="Warehouse" options={warehouses.map((warehouse) => [warehouse.id, warehouse.code])} />
                <Select value={productForm.categoryId} onChange={(categoryId) => setProductForm({ ...productForm, categoryId })} label="Category" options={categories.map((category) => [category.id, category.name])} />
                <textarea maxLength="255" placeholder="Description" value={productForm.description} onChange={(event) => setProductForm({ ...productForm, description: event.target.value })} style={{ minHeight: '60px' }} />
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                  <input required type="number" min="1" placeholder="Unit volume" value={productForm.unitVolume} onChange={(event) => setProductForm({ ...productForm, unitVolume: Number(event.target.value) })} />
                  <input required type="number" min="0" placeholder="Min threshold" value={productForm.reorderThreshold} onChange={(event) => setProductForm({ ...productForm, reorderThreshold: Number(event.target.value) })} />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                  <input required type="number" min="0" step="0.01" placeholder="Price" value={productForm.price} onChange={(event) => setProductForm({ ...productForm, price: Number(event.target.value) })} />
                  <input required type="number" min="0" step="0.01" placeholder="Weight" value={productForm.weight} onChange={(event) => setProductForm({ ...productForm, weight: Number(event.target.value) })} />
                </div>
                <button type="submit" style={{ marginTop: '10px' }}>
                  <Plus size={18} />
                  Add Product
                </button>
              </form>
            </>
          ) : (
            <>
              <PanelTitle icon={Tags} title="Create Category" />
              <form onSubmit={createCategory} className="form-grid" style={{ gap: '10px' }}>
                <input required maxLength="160" placeholder="Category name" value={categoryForm.name} onChange={(event) => setCategoryForm({ ...categoryForm, name: event.target.value })} />
                <Select required value={categoryForm.warehouseId} onChange={(warehouseId) => setCategoryForm({ ...categoryForm, warehouseId })} label="Warehouse" options={warehouses.map((warehouse) => [warehouse.id, warehouse.code])} />
                <Select value={categoryForm.parentCategoryId} onChange={(parentCategoryId) => setCategoryForm({ ...categoryForm, parentCategoryId })} label="Parent category" options={categories.map((category) => [category.id, category.name])} />
                <Select value={categoryForm.preferredZoneId} onChange={(preferredZoneId) => setCategoryForm({ ...categoryForm, preferredZoneId })} label="Preferred zone" options={zones.map((zone) => [zone.id, zone.code])} />
                <button type="submit" style={{ marginTop: '10px' }}><Plus size={18} />Add Category</button>
              </form>
            </>
          )}
        </section>
      )}
      <section className={isAdmin ? "panel" : "panel wide"} style={{ flex: 1 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px', marginBottom: '20px' }}>
          <PanelTitle icon={QrCode} title={`Product Catalog (${filteredProducts.length})`} />
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
            <input 
              type="text" 
              placeholder="Search SKU or Name..." 
              value={searchTerm} 
              onChange={(e) => setSearchTerm(e.target.value)} 
              style={{ width: '200px', padding: '8px 12px', borderRadius: '8px', border: '1px solid var(--border-light)', fontSize: '0.9rem' }}
            />
            <select 
              value={selectedCategory} 
              onChange={(e) => setSelectedCategory(e.target.value)}
              style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid var(--border-light)', background: 'white', fontSize: '0.9rem' }}
            >
              <option value="ALL">All Categories</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
            <div style={{ display: 'flex', border: '1px solid var(--border-light)', borderRadius: '8px', overflow: 'hidden' }}>
              <button 
                type="button" 
                onClick={() => setViewMode('grid')} 
                style={{ padding: '8px', background: viewMode === 'grid' ? 'var(--primary-color)' : 'white', color: viewMode === 'grid' ? 'white' : 'var(--text-secondary)', border: 'none', cursor: 'pointer' }}
                title="Grid View"
              >
                <LayoutGrid size={16} />
              </button>
              <button 
                type="button" 
                onClick={() => setViewMode('table')} 
                style={{ padding: '8px', background: viewMode === 'table' ? 'var(--primary-color)' : 'white', color: viewMode === 'table' ? 'white' : 'var(--text-secondary)', border: 'none', cursor: 'pointer' }}
                title="Table View"
              >
                <List size={16} />
              </button>
            </div>
          </div>
        </div>

        {filteredProducts.length === 0 ? (
          <div className="empty-state" style={{ padding: '40px' }}>
            <QrCode size={48} className="muted" />
            <p>No products found matching your search criteria.</p>
          </div>
        ) : viewMode === 'grid' ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '16px' }}>
            {filteredProducts.map((product) => (
              <div 
                key={product.id} 
                className="product-card" 
                style={{ 
                  background: 'rgba(255, 255, 255, 0.45)', 
                  backdropFilter: 'blur(10px)', 
                  border: '1px solid rgba(255, 255, 255, 0.25)', 
                  borderRadius: '16px', 
                  padding: '16px', 
                  display: 'flex', 
                  flexDirection: 'column', 
                  justifyContent: 'space-between',
                  boxShadow: '0 4px 30px rgba(0, 0, 0, 0.03)',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  cursor: 'pointer'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = 'translateY(-4px)';
                  e.currentTarget.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.08)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = 'translateY(0)';
                  e.currentTarget.style.boxShadow = '0 4px 30px rgba(0, 0, 0, 0.03)';
                }}
              >
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                    <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--primary-color)', background: 'rgba(59, 130, 246, 0.1)', padding: '4px 8px', borderRadius: '6px', letterSpacing: '0.05em' }}>
                      {product.sku}
                    </span>
                    {barcodeUrls[product.id] ? (
                      <img className="qr" src={barcodeUrls[product.id]} alt={`${product.sku} QR code`} style={{ width: '42px', height: '42px', borderRadius: '4px', border: '1px solid #eee' }} />
                    ) : (
                      <span style={{ fontSize: '0.7rem', color: '#999' }}>Secured</span>
                    )}
                  </div>
                  <h4 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-primary)', margin: '0 0 6px 0', lineClamp: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', height: '40px' }}>
                    {product.name}
                  </h4>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '0 0 12px 0' }}>
                    {product.description || 'No description provided.'}
                  </p>
                </div>

                <div style={{ borderTop: '1px solid rgba(0,0,0,0.05)', paddingTop: '12px', marginTop: '8px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Category:</span>
                    <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-primary)' }}>{product.categoryName || 'Unassigned'}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Warehouse:</span>
                    <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-primary)' }}>{product.warehouseCode || 'Unassigned'}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Threshold:</span>
                    <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-primary)' }}>{product.reorderThreshold} units</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '10px' }}>
                    <span style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                      {currency(product.price)}
                    </span>
                    <span style={{ fontSize: '0.7rem', color: '#888' }}>
                      Vol: {product.unitVolume}m³ &bull; Wt: {product.weight}kg
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Barcode</th>
                <th>SKU</th>
                <th>Name</th>
                <th>Category</th>
                <th>Warehouse</th>
                <th>Threshold</th>
                <th>Price</th>
              </tr>
            </thead>
            <tbody>
              {filteredProducts.map((product) => (
                <tr key={product.id}>
                  <td>{barcodeUrls[product.id] ? <img className="qr" src={barcodeUrls[product.id]} alt={`${product.sku} QR code`} /> : <span className="muted">Secured</span>}</td>
                  <td>{product.sku}</td>
                  <td>{product.name}</td>
                  <td>{product.categoryName || 'Unassigned'}</td>
                  <td>{product.warehouseCode || 'Unassigned'}</td>
                  <td>{product.reorderThreshold}</td>
                  <td>{currency(product.price)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
});

const Warehouse = memo(function Warehouse(props) {
  const {
    isAdmin,
    warehouses,
    zones,
    aisles,
    bins,
    zoneForm,
    setZoneForm,
    aisleForm,
    setAisleForm,
    binForm,
    setBinForm,
    createZone,
    createAisle,
    createBin
  } = props;

  return (
    <div className="view-grid">
      {isAdmin && (
        <section className="panel wide">
          <PanelTitle icon={Building2} title="Location Setup" />
          <div className="setup-grid">
            <form onSubmit={createZone} className="form-grid">
              <Select required value={zoneForm.warehouseId} onChange={(warehouseId) => setZoneForm({ ...zoneForm, warehouseId })} label="Warehouse" options={warehouses.map((warehouse) => [warehouse.id, warehouse.code])} />
              <input required pattern="^[A-Za-z0-9._-]+$" maxLength="40" placeholder="Zone code" value={zoneForm.code} onChange={(event) => setZoneForm({ ...zoneForm, code: event.target.value })} />
              <input required maxLength="120" placeholder="Zone name" value={zoneForm.name} onChange={(event) => setZoneForm({ ...zoneForm, name: event.target.value })} />
              <button type="submit"><Plus size={18} />Zone</button>
            </form>
            <form onSubmit={createAisle} className="form-grid">
              <Select required value={aisleForm.zoneId} onChange={(zoneId) => setAisleForm({ ...aisleForm, zoneId })} label="Zone" options={zones.map((zone) => [zone.id, zone.code])} />
              <input required pattern="^[A-Za-z0-9._-]+$" maxLength="40" placeholder="Aisle code" value={aisleForm.code} onChange={(event) => setAisleForm({ ...aisleForm, code: event.target.value })} />
              <button type="submit"><Plus size={18} />Aisle</button>
            </form>
            <form onSubmit={createBin} className="form-grid">
              <Select required value={binForm.aisleId} onChange={(aisleId) => setBinForm({ ...binForm, aisleId })} label="Aisle" options={aisles.map((aisle) => [aisle.id, aisle.code])} />
              <input required pattern="^[A-Za-z0-9._-]+$" maxLength="60" placeholder="Bin code" value={binForm.code} onChange={(event) => setBinForm({ ...binForm, code: event.target.value })} />
              <input required type="number" min="1" placeholder="Capacity" value={binForm.capacity} onChange={(event) => setBinForm({ ...binForm, capacity: Number(event.target.value) })} />
              <button type="submit"><Plus size={18} />Bin</button>
            </form>
          </div>
        </section>
      )}
      <section className="panel wide">
        <PanelTitle icon={Building2} title="Storage Map" />
        <table>
          <thead>
            <tr>
              <th>Bin</th>
              <th>Warehouse</th>
              <th>Zone</th>
              <th>Aisle</th>
              <th>Status</th>
              <th>Capacity</th>
              <th>Available</th>
            </tr>
          </thead>
          <tbody>
            {bins.map((bin) => (
              <tr key={bin.id}>
                <td>{bin.code}</td>
                <td>{bin.warehouseCode}</td>
                <td>{bin.zoneCode}</td>
                <td>{bin.aisleCode}</td>
                <td>{bin.status}</td>
                <td>{bin.capacity}</td>
                <td>{bin.availableCapacity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
});

const Receiving = memo(function Receiving({
  isAdmin,
  products,
  bins,
  inventory,
  receiveForm,
  setReceiveForm,
  adjustForm,
  setAdjustForm,
  transferForm,
  setTransferForm,
  receiveStock,
  adjustStock,
  transferStock
}) {
  const productOptions = products.map((product) => [product.id, `${product.sku} - ${product.name}`]);
  const binOptions = bins.map((bin) => [bin.id, `${bin.code} (${bin.availableCapacity} free)`]);

  return (
    <div className="split-view">
      <section className="panel">
        <PanelTitle icon={ScanBarcode} title="Receive Shipment" />
        <form onSubmit={receiveStock} className="form-grid">
          <Select required value={receiveForm.productId} onChange={(productId) => setReceiveForm({ ...receiveForm, productId })} label="Product" options={productOptions} />
          <input required type="number" min="1" placeholder="Quantity" value={receiveForm.quantity} onChange={(event) => setReceiveForm({ ...receiveForm, quantity: Number(event.target.value) })} />
          <input maxLength="120" placeholder="Reference" value={receiveForm.reference} onChange={(event) => setReceiveForm({ ...receiveForm, reference: event.target.value })} />
          <input maxLength="80" placeholder="Batch number" value={receiveForm.batchNumber} onChange={(event) => setReceiveForm({ ...receiveForm, batchNumber: event.target.value })} />
          <input type="date" value={receiveForm.expiryDate} onChange={(event) => setReceiveForm({ ...receiveForm, expiryDate: event.target.value })} />
          <button type="submit"><CheckCircle2 size={18} />Receive</button>
        </form>
        <div className="operations-grid">
          {isAdmin && (
            <form onSubmit={adjustStock} className="compact-form">
              <PanelTitle icon={Boxes} title="Stock Adjustment" />
              <Select required value={adjustForm.productId} onChange={(productId) => setAdjustForm({ ...adjustForm, productId })} label="Product" options={productOptions} />
              <Select required value={adjustForm.binId} onChange={(binId) => setAdjustForm({ ...adjustForm, binId })} label="Bin" options={binOptions} />
              <input required type="number" placeholder="Delta (+/-)" value={adjustForm.quantityDelta} onChange={(event) => setAdjustForm({ ...adjustForm, quantityDelta: Number(event.target.value) })} />
              <input maxLength="120" placeholder="Reason" value={adjustForm.reason} onChange={(event) => setAdjustForm({ ...adjustForm, reason: event.target.value })} />
              <button type="submit"><Pencil size={18} />Adjust</button>
            </form>
          )}
          <form onSubmit={transferStock} className="compact-form">
            <PanelTitle icon={RefreshCw} title="Bin Transfer" />
            <Select required value={transferForm.productId} onChange={(productId) => setTransferForm({ ...transferForm, productId })} label="Product" options={productOptions} />
            <Select required value={transferForm.fromBinId} onChange={(fromBinId) => setTransferForm({ ...transferForm, fromBinId })} label="From bin" options={binOptions} />
            <Select required value={transferForm.toBinId} onChange={(toBinId) => setTransferForm({ ...transferForm, toBinId })} label="To bin" options={binOptions} />
            <input required type="number" min="1" placeholder="Quantity" value={transferForm.quantity} onChange={(event) => setTransferForm({ ...transferForm, quantity: Number(event.target.value) })} />
            <input maxLength="120" placeholder="Reference" value={transferForm.reference} onChange={(event) => setTransferForm({ ...transferForm, reference: event.target.value })} />
            <button type="submit"><RefreshCw size={18} />Transfer</button>
          </form>
        </div>
        <div className="bin-summary">
          {bins.map((bin) => (
            <span key={bin.id}>{bin.code}: {bin.availableCapacity}</span>
          ))}
        </div>
      </section>
      <section className="panel wide">
        <PanelTitle icon={PackageSearch} title="Current Inventory" />
        <InventoryTable inventory={inventory} />
      </section>
    </div>
  );
});

const Orders = memo(function Orders({ warehouses, products, orders, orderForm, setOrderForm, createOrder, transitionOrder }) {
  return (
    <div className="split-view">
      <section className="panel">
        <PanelTitle icon={ClipboardList} title="Create Order" />
        <form onSubmit={createOrder} className="form-grid">
          <Select required value={orderForm.warehouseId} onChange={(warehouseId) => setOrderForm({ ...orderForm, warehouseId })} label="Warehouse" options={warehouses.map((warehouse) => [warehouse.id, warehouse.code])} />
          <input type="datetime-local" value={orderForm.expectedShipDate} onChange={(event) => setOrderForm({ ...orderForm, expectedShipDate: event.target.value })} />
          <Select required value={orderForm.productId} onChange={(productId) => setOrderForm({ ...orderForm, productId })} label="Product" options={products.map((product) => [product.id, product.sku])} />
          <input required type="number" min="1" placeholder="Quantity" value={orderForm.quantity} onChange={(event) => setOrderForm({ ...orderForm, quantity: Number(event.target.value) })} />
          <button type="submit"><Plus size={18} />Order</button>
        </form>
      </section>
      <section className="panel wide">
        <PanelTitle icon={Truck} title="Fulfillment Queue" />
        <div className="order-list">
          {orders.map((order) => (
            <article key={order.id} className="order-row">
              <div>
                <strong>{order.orderNumber}</strong>
                <span>{order.warehouseCode || 'Warehouse'} - {order.lines.map((line) => `${line.sku} x ${line.requestedQuantity}`).join(', ')}</span>
              </div>
              <b className={`status ${order.status.toLowerCase()}`}>{order.status}</b>
              <div className="row-actions">
                <button onClick={() => transitionOrder(order.id, 'start-picking', 'Order moved to picking.')} disabled={order.status !== 'PENDING'} title="Start picking">
                  <PackageSearch size={16} />
                </button>
                <button onClick={() => transitionOrder(order.id, 'pack', 'Order packed and stock decremented.')} disabled={order.status === 'PACKED' || order.status === 'SHIPPED'} title="Pack order">
                  <Boxes size={16} />
                </button>
                <button onClick={() => transitionOrder(order.id, 'ship', 'Order shipped.')} disabled={order.status !== 'PACKED'} title="Ship order">
                  <Truck size={16} />
                </button>
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
});

const Procurement = memo(function Procurement(props) {
  const {
    isAdmin,
    warehouses,
    products,
    suppliers,
    purchaseOrders,
    supplierForm,
    setSupplierForm,
    purchaseForm,
    setPurchaseForm,
    createSupplier,
    createPurchaseOrder
  } = props;

  return (
    <div className="split-view">
      {isAdmin && (
        <section className="panel">
          <PanelTitle icon={Factory} title="Supplier" />
          <form onSubmit={createSupplier} className="form-grid">
            <input required maxLength="160" placeholder="Supplier name" value={supplierForm.name} onChange={(event) => setSupplierForm({ ...supplierForm, name: event.target.value })} />
            <input maxLength="255" placeholder="Address" value={supplierForm.address} onChange={(event) => setSupplierForm({ ...supplierForm, address: event.target.value })} />
            <input type="email" maxLength="160" placeholder="Contact email" value={supplierForm.contactEmail} onChange={(event) => setSupplierForm({ ...supplierForm, contactEmail: event.target.value })} />
            <input pattern="^[0-9+()\\-\\s]*$" maxLength="40" placeholder="Phone" value={supplierForm.phone} onChange={(event) => setSupplierForm({ ...supplierForm, phone: event.target.value })} />
            <button type="submit"><Plus size={18} />Supplier</button>
          </form>

          <PanelTitle icon={ShoppingCart} title="Purchase Order" />
          <form onSubmit={createPurchaseOrder} className="form-grid">
            <Select required value={purchaseForm.supplierId} onChange={(supplierId) => setPurchaseForm({ ...purchaseForm, supplierId })} label="Supplier" options={suppliers.map((supplier) => [supplier.id, supplier.name])} />
            <Select required value={purchaseForm.warehouseId} onChange={(warehouseId) => setPurchaseForm({ ...purchaseForm, warehouseId })} label="Warehouse" options={warehouses.map((warehouse) => [warehouse.id, warehouse.code])} />
            <input type="datetime-local" value={purchaseForm.expectedDate} onChange={(event) => setPurchaseForm({ ...purchaseForm, expectedDate: event.target.value })} />
            <Select required value={purchaseForm.productId} onChange={(productId) => setPurchaseForm({ ...purchaseForm, productId })} label="Product" options={products.map((product) => [product.id, product.sku])} />
            <input required type="number" min="1" placeholder="Quantity" value={purchaseForm.quantity} onChange={(event) => setPurchaseForm({ ...purchaseForm, quantity: Number(event.target.value) })} />
            <button type="submit"><Plus size={18} />Purchase order</button>
          </form>
        </section>
      )}
      <section className="panel wide">
        <PanelTitle icon={ShoppingCart} title="Purchase Orders" />
        <table>
          <thead>
            <tr>
              <th>PO</th>
              <th>Supplier</th>
              <th>Warehouse</th>
              <th>Status</th>
              <th>Items</th>
            </tr>
          </thead>
          <tbody>
            {purchaseOrders.map((po) => (
              <tr key={po.id}>
                <td>PO-{po.id}</td>
                <td>{po.supplierName}</td>
                <td>{po.warehouseCode}</td>
                <td>{po.status}</td>
                <td>{po.items.map((item) => `${item.sku} x ${item.quantity}`).join(', ')}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <PanelTitle icon={Factory} title="Suppliers" />
        <div className="stack">
          {suppliers.map((supplier) => (
            <div className="order-chip" key={supplier.id}>
              <span>{supplier.name}</span>
              <strong>{supplier.contactEmail || supplier.phone || 'Active'}</strong>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
});

const Users = memo(function Users(props) {
  const {
    users,
    warehouses,
    userForm,
    setUserForm,
    editingUserId,
    saveUser,
    editUser,
    cancelUserEdit
  } = props;

  return (
    <div className="split-view">
      <section className="panel">
        <PanelTitle icon={UserCog} title={editingUserId ? 'Edit User' : 'Create User'} />
        <form onSubmit={saveUser} className="form-grid">
          <input required disabled={Boolean(editingUserId)} pattern="^[A-Za-z0-9][A-Za-z0-9._-]*$" maxLength="80" placeholder="Username" value={userForm.username} onChange={(event) => setUserForm({ ...userForm, username: event.target.value })} />
          <input required={!editingUserId} type="password" minLength="6" maxLength="120" placeholder={editingUserId ? 'New password optional' : 'Password'} value={userForm.password} onChange={(event) => setUserForm({ ...userForm, password: event.target.value })} />
          <input required maxLength="160" placeholder="Full name" value={userForm.name} onChange={(event) => setUserForm({ ...userForm, name: event.target.value })} />
          <input type="email" maxLength="160" placeholder="Email" value={userForm.email} onChange={(event) => setUserForm({ ...userForm, email: event.target.value })} />
          <input pattern="^[0-9+()\\-\\s]*$" maxLength="40" placeholder="Phone" value={userForm.contactNumber} onChange={(event) => setUserForm({ ...userForm, contactNumber: event.target.value })} />
          <Select required value={userForm.role} onChange={(role) => setUserForm({ ...userForm, role })} label="Role" options={[['ADMIN', 'Admin'], ['OPERATOR', 'Operator']]} />
          <Select required value={userForm.status} onChange={(status) => setUserForm({ ...userForm, status })} label="Status" options={[['ACTIVE', 'Active'], ['INACTIVE', 'Inactive']]} />
          <Select value={userForm.warehouseId} onChange={(warehouseId) => setUserForm({ ...userForm, warehouseId })} label="Warehouse" options={warehouses.map((warehouse) => [warehouse.id, warehouse.code])} />
          <div className="button-row">
            <button type="submit"><UserCog size={18} />{editingUserId ? 'Save user' : 'Add user'}</button>
            {editingUserId && <button type="button" className="secondary" onClick={cancelUserEdit}>Cancel</button>}
          </div>
        </form>
      </section>
      <section className="panel wide">
        <PanelTitle icon={UserCog} title="User Access" />
        <table>
          <thead>
            <tr>
              <th>User</th>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Warehouse</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.username}</td>
                <td>{user.name}</td>
                <td>{user.email || '-'}</td>
                <td>{user.role}</td>
                <td><span className={`status ${user.status.toLowerCase()}`}>{user.status}</span></td>
                <td>{user.warehouseCode || '-'}</td>
                <td>
                  <button type="button" title={`Edit ${user.username}`} onClick={() => editUser(user)}>
                    <Pencil size={16} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
});

const Metric = memo(function Metric({ icon: Icon, label, value }) {
  return (
    <div className="metric">
      <Icon size={24} />
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
});

const PanelTitle = memo(function PanelTitle({ icon: Icon, title }) {
  return (
    <div className="panel-title">
      <Icon size={19} />
      <h3>{title}</h3>
    </div>
  );
});

const InventoryTable = memo(function InventoryTable({ inventory }) {
  return (
    <table>
      <thead>
        <tr>
          <th>SKU</th>
          <th>Product</th>
          <th>Bin</th>
          <th>Warehouse</th>
          <th>Status</th>
          <th>Qty</th>
          <th>Available</th>
        </tr>
      </thead>
      <tbody>
        {inventory.map((row) => (
          <tr key={`${row.productId}-${row.binId}`}>
            <td>{row.sku}</td>
            <td>{row.productName}</td>
            <td>{row.binCode}</td>
            <td>{row.warehouseCode}</td>
            <td>{row.status}</td>
            <td>{row.quantity}</td>
            <td>{row.availableQuantity}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
});

const Select = memo(function Select({ value, onChange, label, options, required = false }) {
  return (
    <select required={required} value={value} onChange={(event) => onChange(event.target.value)} aria-label={label}>
      <option value="">{label}</option>
      {options.map(([id, text]) => (
        <option key={id} value={id}>{text}</option>
      ))}
    </select>
  );
});

function normalizeIds(object, idFields) {
  return Object.fromEntries(
    Object.entries(object).map(([key, value]) => {
      if (idFields.includes(key)) {
        return [key, value === '' || value === null || value === undefined ? null : Number(value)];
      }
      return [key, value === '' ? null : value];
    })
  );
}

function cleanDates(object, dateFields) {
  return Object.fromEntries(
    Object.entries(object).map(([key, value]) => {
      if (dateFields.includes(key) && value) {
        const lowerKey = key.toLowerCase();
        if ((lowerKey.includes('ship') || lowerKey.includes('date')) && value.includes('T')) {
          return [key, new Date(value).toISOString()];
        }
      }
      return [key, value];
    })
  );
}

function currency(value) {
  if (value === null || value === undefined) {
    return '-';
  }
  return Number(value).toLocaleString('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 });
}

export default App;
