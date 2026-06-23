import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [metricas, setMetricas] = useState({
    cuentas: 820000,
    saldoTotal: 0,
    transferencias: 0,
    ultimaTx: "Ninguna",
    ramUsoPorcentaje: 0
  });

  // Estados principales para el panel de transferencia
  const [origen, setOrigen] = useState("1");
  const [destino, setDestino] = useState("2");
  const [monto, setMonto] = useState("500");

  // Estados para los monitores (inicializados con los valores del panel)
  const [mon1, setMon1] = useState("1");
  const [mon2, setMon2] = useState("2");
  const [mon3, setMon3] = useState("3");

  const [datosMon1, setDatosMon1] = useState("cargando...");
  const [datosMon2, setDatosMon2] = useState("cargando...");
  const [datosMon3, setDatosMon3] = useState("cargando...");

  const cargarDatos = () => {
    fetch('http://localhost:8080/api/metrics')
      .then(res => res.json())
      .then(data => setMetricas(data))
      .catch(console.error);

    const cargarCuenta = (id: string, setter: Function) => {
      if (!id) return;
      fetch(`http://localhost:8080/api/accounts/${id}`)
        .then(res => res.json())
        .then(data => setter(`${data.propietario} - $${data.balance.toFixed(2)}`))
        .catch(() => setter("Error: Cuenta no existe"));
    };

    cargarCuenta(mon1, setDatosMon1);
    cargarCuenta(mon2, setDatosMon2);
    cargarCuenta(mon3, setDatosMon3);
  };

  useEffect(() => {
    cargarDatos();
    const int = setInterval(cargarDatos, 1000);
    return () => clearInterval(int);
  }, [mon1, mon2, mon3]);

  const enviarTransferencia = () => {
    fetch('http://localhost:8080/api/transactions/transfer', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sourceAccountId: origen, targetAccountId: destino, amount: parseFloat(monto) })
    })
    .then(res => res.json())
    .then(data => alert(data.mensaje || data.error))
    .catch(() => alert("Error de conexión"));
  };

  // Sincronización automática: si cambias el ID en el panel, se actualiza el monitor
  const manejarCambioOrigen = (val: string) => { setOrigen(val); setMon1(val); };
  const manejarCambioDestino = (val: string) => { setDestino(val); setMon2(val); };

  const renderCard = (titulo: string, monitor: string, setMonitor: Function, datos: string, cpu: number, disco: number) => (
    <div className="card">
      <h2>{titulo}</h2>
      <p className="estado-activo">Activo</p>
      <p>Saldo total: ${metricas.saldoTotal.toFixed(2)}</p>
      <p>Transferencias: {metricas.transferencias}</p>
      <p>CPU: {cpu}% | RAM: {metricas.ramUsoPorcentaje.toFixed(2)}%</p>
      <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#0a0d12', borderRadius: '5px' }}>
        <input value={monitor} onChange={(e) => setMonitor(e.target.value)} style={{ width: '90%', marginBottom: '5px', background: 'transparent', border: '1px solid #333', color: 'white' }} />
        <span style={{ fontSize: '12px', display: 'block' }}>{datos}</span>
      </div>
    </div>
  );

  return (
    <div>
      <h1>Mini Banco Distribuido</h1>
      <div style={{ background: '#12161d', padding: '20px', borderRadius: '10px', marginBottom: '20px', display: 'flex', gap: '20px' }}>
        <input placeholder="ID Origen" value={origen} onChange={(e) => manejarCambioOrigen(e.target.value)} />
        <input placeholder="ID Destino" value={destino} onChange={(e) => manejarCambioDestino(e.target.value)} />
        <input type="number" value={monto} onChange={(e) => setMonto(e.target.value)} />
        <button onClick={enviarTransferencia}>Enviar Transferencia</button>
      </div>

      <div className="contenedor-tarjetas">
        {renderCard("nodo-1 (Líder)", mon1, setMon1, datosMon1, 42, 30)}
        {renderCard("nodo-2 (Réplica)", mon2, setMon2, datosMon2, 25, 28)}
        {renderCard("nodo-3 (Réplica)", mon3, setMon3, datosMon3, 33, 31)}
      </div>
    </div>
  );
}

export default App;