import React, { useState, useEffect } from 'react';
import './App.css';

//CONEXIÓN A AWS 
const IP_LIDER = "35.153.159.152";
const BASE_URL = `http://${IP_LIDER}:8080`;

function App() {
  const [metricas, setMetricas] = useState({
    cuentas: 820000,
    saldoTotal: 0,
    transferencias: 0,
    ultimaTx: "Ninguna",
    ramUsoPorcentaje: 0,
    cpuUsoPorcentaje: 0, // metrica del backend
    discoUsoPorcentaje: 0 //  metrica del backend
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
    fetch(`${BASE_URL}/api/metrics`)
      .then(res => res.json())
      .then(data => setMetricas(data))
      .catch(console.error);

    const cargarCuenta = (id: string, setter: React.Dispatch<React.SetStateAction<string>>) => {
      if (!id) return;
      fetch(`${BASE_URL}/api/accounts/${id}`)
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
    if (!origen || !destino || !monto) {
      alert("Por favor llena todos los campos");
      return;
    }

<<<<<<< HEAD
    fetch(`${BASE_URL}/api/transactions/transfer`, {
=======
    fetch('http://localhost:8080/api/transactions/transfer', {
>>>>>>> 3f2da2a855dc431f89b9b249fcc607d1a0f69290
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sourceAccountId: origen, targetAccountId: destino, amount: parseFloat(monto) })
    })
    .then(res => res.json())
    .then(data => alert(data.mensaje || data.error))
    .catch(() => alert("Error de conexión al transferir"));
  };

  // Sincronización automática: si cambias el ID en el panel, se actualiza el monitor
  const manejarCambioOrigen = (val: string) => { setOrigen(val); setMon1(val); };
  const manejarCambioDestino = (val: string) => { setDestino(val); setMon2(val); };

  // Renderizado de las tarjetas con formato de moneda y etiquetas dinámicas
  const renderCard = (
    titulo: string, 
    monitor: string, 
    setMonitor: React.Dispatch<React.SetStateAction<string>>, 
    datos: string, 
<<<<<<< HEAD
=======
    cpu: number, 
    disco: number, 
>>>>>>> 3f2da2a855dc431f89b9b249fcc607d1a0f69290
    etiquetaBusqueda: string = "Monitorear Cuenta ID:"
  ) => (
    <div className="card">
      <h2>{titulo}</h2>
      <p className="estado-activo">Activo</p>
      
      {/* Formato de moneda con separadores de miles y decimales */}
      <p>Saldo total: ${metricas.saldoTotal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
      
      <p>Transferencias: {metricas.transferencias}</p>
<<<<<<< HEAD
      {/* Las métricas ahora son dinámicas y vienen del backend */}
      <p>CPU: {metricas.cpuUsoPorcentaje.toFixed(2)}% | RAM: {metricas.ramUsoPorcentaje.toFixed(2)}% | Disco: {metricas.discoUsoPorcentaje.toFixed(2)}%</p>
=======
      <p>CPU: {cpu}% | RAM: {metricas.ramUsoPorcentaje.toFixed(2)}%</p>
>>>>>>> 3f2da2a855dc431f89b9b249fcc607d1a0f69290
      
      <div style={{ marginTop: '15px', padding: '12px', backgroundColor: '#0a0d12', borderRadius: '8px', border: '1px solid #1f2530' }}>
        <label style={{ display: 'block', fontSize: '11px', color: '#60a5fa', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          {etiquetaBusqueda}
        </label>
        <input 
          value={monitor} 
          onChange={(e) => setMonitor(e.target.value)} 
          style={{ width: '90%', padding: '6px', marginBottom: '8px', backgroundColor: '#12161d', border: '1px solid #333', color: 'white', borderRadius: '4px' }} 
        />
        <span style={{ fontSize: '13px', display: 'block', fontWeight: 'bold' }}>{datos}</span>
      </div>
    </div>
  );

  return (
    <div>
      <h1 style={{ marginBottom: '30px' }}>Mini Banco Distribuido</h1>
      
      {/* Panel de Control de Transferencias */}
      <div style={{ 
        background: '#12161d', 
        padding: '20px 25px', 
        borderRadius: '12px', 
        marginBottom: '30px', 
        display: 'flex', 
        gap: '20px', 
        alignItems: 'flex-end',
        border: '1px solid #1f2530',
        boxShadow: '0 4px 6px rgba(0,0,0,0.3)'
      }}>
        
        {/* Bloque Origen */}
        <div>
          <label style={{ display: 'block', fontSize: '13px', color: '#60a5fa', marginBottom: '8px', fontWeight: 'bold' }}>
            📤 Cuenta Origen
          </label>
          <input 
            type="text" 
            placeholder="ID Origen" 
            value={origen} 
            onChange={(e) => manejarCambioOrigen(e.target.value)} 
            style={{ width: '120px', padding: '10px', borderRadius: '6px', border: '1px solid #333', backgroundColor: '#0a0d12', color: 'white', fontSize: '15px' }}
          />
        </div>

        {/* Flecha visual */}
        <div style={{ paddingBottom: '12px', fontSize: '20px', opacity: 0.7 }}>
          ➡️
        </div>

        {/* Bloque Destino */}
        <div>
          <label style={{ display: 'block', fontSize: '13px', color: '#60a5fa', marginBottom: '8px', fontWeight: 'bold' }}>
            📥 Cuenta Destino
          </label>
          <input 
            type="text" 
            placeholder="ID Destino" 
            value={destino} 
            onChange={(e) => manejarCambioDestino(e.target.value)} 
            style={{ width: '120px', padding: '10px', borderRadius: '6px', border: '1px solid #333', backgroundColor: '#0a0d12', color: 'white', fontSize: '15px' }}
          />
        </div>

        {/* Separador visual */}
        <div style={{ width: '1px', height: '40px', backgroundColor: '#333', margin: '0 10px', alignSelf: 'center' }}></div>

        {/* Bloque Monto */}
        <div>
          <label style={{ display: 'block', fontSize: '13px', color: '#34d399', marginBottom: '8px', fontWeight: 'bold' }}>
            💲 Monto
          </label>
          <input 
            type="number" 
            value={monto} 
            onChange={(e) => setMonto(e.target.value)} 
            style={{ width: '120px', padding: '10px', borderRadius: '6px', border: '1px solid #34d399', backgroundColor: '#0a0d12', color: 'white', fontSize: '15px' }}
          />
        </div>

        {/* Botón de envío */}
        <button 
          onClick={enviarTransferencia}
          style={{ 
            padding: '12px 24px', 
            cursor: 'pointer', 
            backgroundColor: '#34d399', 
            color: '#0a0d12', 
            border: 'none', 
            borderRadius: '6px', 
            fontWeight: 'bold',
            fontSize: '15px',
            marginLeft: 'auto',
            transition: '0.2s'
          }}
        >
           Ejecutar Transferencia
        </button>
      </div>

      {/* Renderizado de los Nodos */}
      <div className="contenedor-tarjetas">
<<<<<<< HEAD
        {/* eliminamos los números quemados  en la llamada a renderCard */}
        {renderCard("nodo-1 (Líder)", mon1, setMon1, datosMon1, "Monitorear Origen:")}
        {renderCard("nodo-2 (Réplica)", mon2, setMon2, datosMon2, "Monitorear Destino:")}
        {renderCard("nodo-3 (Réplica)", mon3, setMon3, datosMon3, "🔍 Búsqueda Libre (Auditoría):")}
=======
        {renderCard("nodo-1 (Líder)", mon1, setMon1, datosMon1, 42, 30, "Monitorear Origen:")}
        {renderCard("nodo-2 (Réplica)", mon2, setMon2, datosMon2, 25, 28, "Monitorear Destino:")}
        {renderCard("nodo-3 (Réplica)", mon3, setMon3, datosMon3, 33, 31, "🔍 Búsqueda Libre (Auditoría):")}
>>>>>>> 3f2da2a855dc431f89b9b249fcc607d1a0f69290
      </div>
    </div>
  );
}

export default App;