const API_ROOT = "/api/v1";
let csrfToken;

export class ApiError extends Error {
    constructor(message, status) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

async function loadCsrfToken() {
    const response = await fetch(`${API_ROOT}/auth/csrf`, {
        headers: { Accept: "application/json" },
        credentials: "same-origin",
    });
    if (!response.ok) {
        throw new ApiError("Não foi possível iniciar uma sessão segura.", response.status);
    }
    const body = await response.json();
    csrfToken = body.token;
    return body.token;
}

async function request(path, options = {}) {
    const method = (options.method || "GET").toUpperCase();
    const headers = new Headers(options.headers || {});
    headers.set("Accept", "application/json");

    if (options.body !== undefined) {
        headers.set("Content-Type", "application/json");
    }
    if (!["GET", "HEAD", "OPTIONS", "TRACE"].includes(method)) {
        if (!csrfToken) {
            await loadCsrfToken();
        }
        headers.set("X-XSRF-TOKEN", csrfToken);
    }

    const response = await fetch(`${API_ROOT}${path}`, {
        ...options,
        method,
        headers,
        credentials: "same-origin",
    });

    if (response.status === 204) {
        return null;
    }
    if (!response.ok) {
        let message = `O pedido falhou (${response.status}).`;
        try {
            const body = await response.json();
            message = body.mensagem || body.message || message;
        } catch {
            if (response.status === 401) {
                message = "A sessão terminou. Inicie sessão novamente.";
            } else if (response.status === 403) {
                message = "A sua conta não tem permissão para esta operação.";
            }
        }
        throw new ApiError(message, response.status);
    }
    return response.json();
}

export const api = {
    get: (path) => request(path),
    post: (path, body) => request(path, {
        method: "POST",
        body: JSON.stringify(body),
    }),
    async login(nomeUtilizador, senha) {
        await loadCsrfToken();
        const account = await request("/auth/login", {
            method: "POST",
            body: JSON.stringify({ nomeUtilizador, senha }),
        });
        await loadCsrfToken();
        return account;
    },
    async logout() {
        await request("/auth/logout", { method: "POST" });
        csrfToken = undefined;
    },
    me: () => request("/auth/me"),
};
