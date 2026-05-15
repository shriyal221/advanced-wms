const https = require('https');
const zlib = require('zlib');
const fs = require('fs');

const mermaidCode = `graph TD
    classDef entity fill:#ffcc99,stroke:#e67300,stroke-width:2px,color:#000;
    classDef relationship fill:#85e085,stroke:#2eb82e,stroke-width:2px,color:#000;
    classDef attribute fill:#ffe6cc,stroke:#ffb366,stroke-width:1px,color:#000;

    Warehouse[Warehouse]:::entity
    Zone[Zone]:::entity
    Aisle[Aisle]:::entity
    StorageBin[StorageBin]:::entity
    ProductCategory[Product Category]:::entity
    Product[Product]:::entity
    InventoryItem[InventoryItem]:::entity
    InventoryTransaction[Inv. Transaction]:::entity
    Supplier[Supplier]:::entity
    PurchaseOrder[Purchase Order]:::entity
    PurchaseOrderItem[PO Item]:::entity
    CustomerOrder[Customer Order]:::entity
    CustomerOrderLine[Order Line]:::entity
    AppUser[User]:::entity

    relContains{contains}:::relationship
    relGroups{groups}:::relationship
    relHas{has}:::relationship
    relHolds{stores}:::relationship
    relLogs{logs}:::relationship
    relCategorizes{categorizes}:::relationship
    relStocked{stocked as}:::relationship
    relTracked{tracked via}:::relationship
    relSupplies{supplies}:::relationship
    relIncludesPO{includes}:::relationship
    relOrderedIn{ordered in}:::relationship
    relIncludesCO{includes}:::relationship
    relSoldIn{sold in}:::relationship
    relFulfills{fulfills}:::relationship
    relReceives{receives}:::relationship
    relEmploys{employs}:::relationship

    Warehouse ---|1| relContains ---|M| Zone
    Zone ---|1| relGroups ---|M| Aisle
    Aisle ---|1| relHas ---|M| StorageBin
    StorageBin ---|1| relHolds ---|M| InventoryItem
    StorageBin ---|1| relLogs ---|M| InventoryTransaction
    ProductCategory ---|1| relCategorizes ---|M| Product
    Product ---|1| relStocked ---|M| InventoryItem
    Product ---|1| relTracked ---|M| InventoryTransaction
    Supplier ---|1| relSupplies ---|M| PurchaseOrder
    PurchaseOrder ---|1| relIncludesPO ---|M| PurchaseOrderItem
    Product ---|1| relOrderedIn ---|M| PurchaseOrderItem
    Warehouse ---|1| relFulfills ---|M| CustomerOrder
    CustomerOrder ---|1| relIncludesCO ---|M| CustomerOrderLine
    Product ---|1| relSoldIn ---|M| CustomerOrderLine
    Warehouse ---|1| relReceives ---|M| PurchaseOrder
    Warehouse ---|1| relEmploys ---|M| AppUser

    W_id([id]):::attribute
    W_code([code]):::attribute
    W_name([name]):::attribute
    W_loc([location]):::attribute
    W_active([active]):::attribute
    Warehouse --- W_id & W_code & W_name & W_loc & W_active

    Z_id([id]):::attribute
    Z_code([code]):::attribute
    Z_name([name]):::attribute
    Z_active([active]):::attribute
    Zone --- Z_id & Z_code & Z_name & Z_active

    A_id([id]):::attribute
    A_code([code]):::attribute
    A_active([active]):::attribute
    Aisle --- A_id & A_code & A_active

    SB_id([id]):::attribute
    SB_code([code]):::attribute
    SB_cap([capacity]):::attribute
    SB_used([usedCapacity]):::attribute
    StorageBin --- SB_id & SB_code & SB_cap & SB_used

    PC_id([id]):::attribute
    PC_name([name]):::attribute
    PC_desc([description]):::attribute
    ProductCategory --- PC_id & PC_name & PC_desc

    P_id([id]):::attribute
    P_sku([sku]):::attribute
    P_name([name]):::attribute
    P_barcode([barcode]):::attribute
    P_desc([description]):::attribute
    P_uv([unitVolume]):::attribute
    P_rt([minThreshold]):::attribute
    P_price([price]):::attribute
    P_weight([weight]):::attribute
    P_active([active]):::attribute
    Product --- P_id & P_sku & P_name & P_barcode & P_desc & P_uv & P_rt & P_price & P_weight & P_active

    II_id([id]):::attribute
    II_qty([quantity]):::attribute
    II_rqty([reservedQty]):::attribute
    II_batch([batchNumber]):::attribute
    II_exp([expiryDate]):::attribute
    II_status([status]):::attribute
    InventoryItem --- II_id & II_qty & II_rqty & II_batch & II_exp & II_status

    IT_id([id]):::attribute
    IT_type([type]):::attribute
    IT_qty([quantity]):::attribute
    IT_ref([reference]):::attribute
    IT_date([transactionDate]):::attribute
    InventoryTransaction --- IT_id & IT_type & IT_qty & IT_ref & IT_date

    S_id([id]):::attribute
    S_name([name]):::attribute
    S_addr([address]):::attribute
    S_email([contactEmail]):::attribute
    S_phone([phone]):::attribute
    Supplier --- S_id & S_name & S_addr & S_email & S_phone

    PO_id([id]):::attribute
    PO_exp([expectedDate]):::attribute
    PO_status([status]):::attribute
    PurchaseOrder --- PO_id & PO_exp & PO_status

    POI_id([id]):::attribute
    POI_qty([quantity]):::attribute
    PurchaseOrderItem --- POI_id & POI_qty

    CO_id([id]):::attribute
    CO_num([orderNumber]):::attribute
    CO_status([status]):::attribute
    CO_cdate([createdAt]):::attribute
    CO_edate([expectedShipDate]):::attribute
    CO_pdate([packedAt]):::attribute
    CustomerOrder --- CO_id & CO_num & CO_status & CO_cdate & CO_edate & CO_pdate

    COL_id([id]):::attribute
    COL_rqty([requestedQty]):::attribute
    COL_pqty([pickedQty]):::attribute
    CustomerOrderLine --- COL_id & COL_rqty & COL_pqty

    U_id([id]):::attribute
    U_user([username]):::attribute
    U_role([role]):::attribute
    U_name([name]):::attribute
    U_email([email]):::attribute
    U_contact([contactNumber]):::attribute
    U_status([status]):::attribute
    AppUser --- U_id & U_user & U_role & U_name & U_email & U_contact & U_status
`;

const data = Buffer.from(mermaidCode, 'utf8');
const compressed = zlib.deflateSync(data);
const b64 = compressed.toString('base64').replace(/\+/g, '-').replace(/\//g, '_');

const url = 'https://kroki.io/mermaid/png/' + b64;
console.log('Downloading from: ' + url);

https.get(url, (res) => {
  if (res.statusCode !== 200) {
    console.error('Failed with status: ' + res.statusCode);
    res.resume();
    return;
  }
  const file = fs.createWriteStream('erd.png');
  res.pipe(file);
  file.on('finish', () => {
    file.close();
    console.log('Download completed');
  });
}).on('error', (err) => {
  console.error('Error: ' + err.message);
});
