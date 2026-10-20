const API = 'http://localhost:8080/api/usuarios';

function mostrar(data) {
    document.getElementById('resultado').textContent = JSON.stringify(data, null, 2);
}

function actualizarSesion() {
    const token = localStorage.getItem('token');
    document.getElementById('sesion').textContent = token
        ? 'Sesión activa (token guardado)'
        : 'Sin sesión iniciada';
}

async function peticion(url, metodo, body, autenticado = false) {
    const headers = { 'Content-Type': 'application/json' };
    if (autenticado) {
        const token = localStorage.getItem('token');
        headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(url, {
        method: metodo,
        headers,
        body: body ? JSON.stringify(body) : undefined
    });

    let data;
    try {
        data = await res.json();
    } catch {
        data = { status: res.status, info: 'Sin contenido en la respuesta' };
    }
    mostrar(data);
    return { ok: res.ok, data };
}

async function registrar() {
    await peticion(`${API}/registro`, 'POST', {
        nombre: document.getElementById('reg-nombre').value,
        email: document.getElementById('reg-email').value,
        password: document.getElementById('reg-password').value,
        telefono: document.getElementById('reg-telefono').value
    });
}

async function login() {
    const { ok, data } = await peticion(`${API}/login`, 'POST', {
        email: document.getElementById('login-email').value,
        password: document.getElementById('login-password').value
    });
    if (ok && data.token) {
        localStorage.setItem('token', data.token);
        actualizarSesion();
    }
}

async function verPerfil() {
    await peticion(`${API}/me`, 'GET', null, true);
}

async function actualizarPerfil() {
    await peticion(`${API}/me`, 'PUT', {
        nombre: document.getElementById('perfil-nombre').value,
        telefono: document.getElementById('perfil-telefono').value
    }, true);
}

async function convertirseVendedor() {
    await peticion(`${API}/me/vendedor`, 'POST', null, true);
}

async function listarUsuarios() {
    await peticion(`${API}`, 'GET', null, true);
}

async function desactivarUsuario() {
    const id = document.getElementById('admin-id-desactivar').value;
    if (!id) {
        mostrar({ error: 'Escribe un ID de usuario' });
        return;
    }
    await peticionSinBody(`${API}/${id}`, 'DELETE', true);
}

async function reactivarUsuario() {
    const id = document.getElementById('admin-id-reactivar').value;
    if (!id) {
        mostrar({ error: 'Escribe un ID de usuario' });
        return;
    }
    await peticionSinBody(`${API}/${id}/reactivar`, 'PATCH', true);
}

async function peticionSinBody(url, metodo, autenticado = false) {
    const headers = {};
    if (autenticado) {
        const token = localStorage.getItem('token');
        headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(url, { method: metodo, headers });

    if (res.status === 204) {
        mostrar({ info: 'Usuario desactivado correctamente' });
        return;
    }

    let data;
    try {
        data = await res.json();
    } catch {
        data = { status: res.status };
    }
    mostrar(data);
}

actualizarSesion();