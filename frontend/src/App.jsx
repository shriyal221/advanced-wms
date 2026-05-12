import {
  Boxes,
  Building2,
  CheckCircle2,
  ClipboardList,
  Factory,
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
  UserCog
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
  const [warehouseForm, setWarehouseForm] = useState({ code: '', name: '', address: '' });
  const [zoneForm, setZoneForm] = useState({ warehouseId: '', code: '', name: '' });
  const [aisleForm, setAisleForm] = useState({ zoneId: '', code: '' });
  const [binForm, setBinForm] = useState({ aisleId: '', code: '', capacity: 100 });
  const [receiveForm, setReceiveForm] = useState({ productId: '', quantity: 1, reference: '', batchNumber: '', expiryDate: '' });
  const [orderForm, setOrderForm] = useState({ warehouseId: '', expectedShipDate: '', productId: '', quantity: 1 });
  const [supplierForm, setSupplierForm] = useState({ name: '', address: '', contactEmail: '', phone: '' });
  const [purchaseForm, setPurchaseForm] = useState({ supplierId: '', warehouseId: '', expectedDate: '', productId: '', quantity: 1 });
  const [userForm, setUserForm] = useState(blankUser);
  const [editingUserId, setEditingUserId] = useState(null);

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
        nextUsers
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
        isAdmin ? apiRequest('/users', { token }) : Promise.resolve([])
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

  async function createWarehouse(event) {
    event.preventDefault();
    await run(async () => {
      await apiRequest('/warehouses', { method: 'POST', token, body: warehouseForm });
      setWarehouseForm({ code: '', name: '', address: '' });
      await loadAll();
    }, 'Warehouse created.');
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
      <main className="login-shell">
        <section className="login-panel">
          <div className="brand-line">
            <ShieldCheck size={30} />
            <div>
              <p>Infotact WMS</p>
              <h1>Advanced Warehouse Management</h1>
            </div>
          </div>
          <form onSubmit={login} className="login-form">
            <label>
              Username
              <input autoComplete="username" required value={loginForm.username} onChange={(event) => setLoginForm({ ...loginForm, username: event.target.value })} />
            </label>
            <label>
              Password
              <input autoComplete="current-password" required type="password" value={loginForm.password} onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })} />
            </label>
            <button type="submit" disabled={loading}>
              <ShieldCheck size={18} />
              Sign in
            </button>
          </form>
          {message && <p className="message error">{message}</p>}
        </section>
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
            <p className="eyebrow">{auth.roles.join(', ')}</p>
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

        {activeTab === 'dashboard' && <Dashboard totals={totals} inventory={inventory} bins={bins} orders={orders} purchaseOrders={purchaseOrders} />}
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
            warehouseForm={warehouseForm}
            setWarehouseForm={setWarehouseForm}
            zoneForm={zoneForm}
            setZoneForm={setZoneForm}
            aisleForm={aisleForm}
            setAisleForm={setAisleForm}
            binForm={binForm}
            setBinForm={setBinForm}
            createWarehouse={createWarehouse}
            createZone={createZone}
            createAisle={createAisle}
            createBin={createBin}
          />
        )}
        {activeTab === 'receiving' && (
          <Receiving
            products={products}
            bins={bins}
            inventory={inventory}
            receiveForm={receiveForm}
            setReceiveForm={setReceiveForm}
            receiveStock={receiveStock}
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

const Dashboard = memo(function Dashboard({ totals, inventory, bins, orders, purchaseOrders }) {
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

  return (
    <div className="split-view">
      {isAdmin && (
        <section className="panel">
          <PanelTitle icon={PackagePlus} title="Create Product" />
          <form onSubmit={createProduct} className="form-grid">
            <input required pattern="^[A-Za-z0-9][A-Za-z0-9._-]*$" maxLength="80" placeholder="SKU" value={productForm.sku} onChange={(event) => setProductForm({ ...productForm, sku: event.target.value })} />
            <input required maxLength="160" placeholder="Name" value={productForm.name} onChange={(event) => setProductForm({ ...productForm, name: event.target.value })} />
            <input pattern="^[A-Za-z0-9._-]*$" maxLength="120" placeholder="Barcode" value={productForm.barcode} onChange={(event) => setProductForm({ ...productForm, barcode: event.target.value })} />
            <Select required value={productForm.warehouseId} onChange={(warehouseId) => setProductForm({ ...productForm, warehouseId })} label="Warehouse" options={warehouses.map((warehouse) => [warehouse.id, warehouse.code])} />
            <Select value={productForm.categoryId} onChange={(categoryId) => setProductForm({ ...productForm, categoryId })} label="Category" options={categories.map((category) => [category.id, category.name])} />
            <textarea maxLength="255" placeholder="Description" value={productForm.description} onChange={(event) => setProductForm({ ...productForm, description: event.target.value })} />
            <input required type="number" min="1" placeholder="Unit volume" value={productForm.unitVolume} onChange={(event) => setProductForm({ ...productForm, unitVolume: Number(event.target.value) })} />
            <input required type="number" min="0" placeholder="Min threshold" value={productForm.reorderThreshold} onChange={(event) => setProductForm({ ...productForm, reorderThreshold: Number(event.target.value) })} />
            <input required type="number" min="0" step="0.01" placeholder="Price" value={productForm.price} onChange={(event) => setProductForm({ ...productForm, price: Number(event.target.value) })} />
            <input required type="number" min="0" step="0.01" placeholder="Weight" value={productForm.weight} onChange={(event) => setProductForm({ ...productForm, weight: Number(event.target.value) })} />
            <button type="submit">
              <Plus size={18} />
              Add product
            </button>
          </form>

          <PanelTitle icon={Tags} title="Create Category" />
          <form onSubmit={createCategory} className="form-grid">
            <input required maxLength="160" placeholder="Category name" value={categoryForm.name} onChange={(event) => setCategoryForm({ ...categoryForm, name: event.target.value })} />
            <Select required value={categoryForm.warehouseId} onChange={(warehouseId) => setCategoryForm({ ...categoryForm, warehouseId })} label="Warehouse" options={warehouses.map((warehouse) => [warehouse.id, warehouse.code])} />
            <Select value={categoryForm.parentCategoryId} onChange={(parentCategoryId) => setCategoryForm({ ...categoryForm, parentCategoryId })} label="Parent category" options={categories.map((category) => [category.id, category.name])} />
            <Select value={categoryForm.preferredZoneId} onChange={(preferredZoneId) => setCategoryForm({ ...categoryForm, preferredZoneId })} label="Preferred zone" options={zones.map((zone) => [zone.id, zone.code])} />
            <button type="submit"><Plus size={18} />Add category</button>
          </form>
        </section>
      )}
      <section className={isAdmin ? "panel" : "panel wide"}>
        <PanelTitle icon={QrCode} title="Product Catalog" />
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
            {products.map((product) => (
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
    warehouseForm,
    setWarehouseForm,
    zoneForm,
    setZoneForm,
    aisleForm,
    setAisleForm,
    binForm,
    setBinForm,
    createWarehouse,
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
            <form onSubmit={createWarehouse} className="form-grid">
              <input required pattern="^[A-Za-z0-9._-]+$" maxLength="40" placeholder="Warehouse code" value={warehouseForm.code} onChange={(event) => setWarehouseForm({ ...warehouseForm, code: event.target.value })} />
              <input required maxLength="160" placeholder="Warehouse name" value={warehouseForm.name} onChange={(event) => setWarehouseForm({ ...warehouseForm, name: event.target.value })} />
              <input maxLength="255" placeholder="Address" value={warehouseForm.address} onChange={(event) => setWarehouseForm({ ...warehouseForm, address: event.target.value })} />
              <button type="submit"><Plus size={18} />Warehouse</button>
            </form>
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

const Receiving = memo(function Receiving({ products, bins, inventory, receiveForm, setReceiveForm, receiveStock }) {
  return (
    <div className="split-view">
      <section className="panel">
        <PanelTitle icon={ScanBarcode} title="Receive Shipment" />
        <form onSubmit={receiveStock} className="form-grid">
          <Select required value={receiveForm.productId} onChange={(productId) => setReceiveForm({ ...receiveForm, productId })} label="Product" options={products.map((product) => [product.id, product.sku])} />
          <input required type="number" min="1" placeholder="Quantity" value={receiveForm.quantity} onChange={(event) => setReceiveForm({ ...receiveForm, quantity: Number(event.target.value) })} />
          <input maxLength="120" placeholder="Reference" value={receiveForm.reference} onChange={(event) => setReceiveForm({ ...receiveForm, reference: event.target.value })} />
          <input maxLength="80" placeholder="Batch number" value={receiveForm.batchNumber} onChange={(event) => setReceiveForm({ ...receiveForm, batchNumber: event.target.value })} />
          <input type="date" value={receiveForm.expiryDate} onChange={(event) => setReceiveForm({ ...receiveForm, expiryDate: event.target.value })} />
          <button type="submit"><CheckCircle2 size={18} />Receive</button>
        </form>
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
