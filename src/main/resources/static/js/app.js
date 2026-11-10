import { api, ApiError } from "./api.js";

const PAGE_SIZE = 10;
const state = {
    user: null,
    view: "dashboard",
    clients: [],
    meters: [],
    page: 1,
};

const workspace = document.querySelector("#workspace");
const navigation = document.querySelector("#main-navigation");
const dialog = document.querySelector("#app-dialog");
const dialogForm = document.querySelector("#dialog-form");
const dialogFields = document.querySelector("#dialog-fields");
let dialogAction = null;
let dialogBusy = false;

const roleLabels = {
    ADMIN: "Administrador",
    LEITOR: "Leitor de contadores",
    CLIENTE: "Cliente",
};

const navGroups = {
    staff: [
        {
            title: "OPERAÇÕES",
            items: [
                ["dashboard", "Visão geral", "M4 13h6V3H4v10Zm10 8h6V11h-6v10ZM4 21h6v-4H4v4Zm10-14h6V3h-6v4Z"],
                ["leituras", "Leituras", "M12 3v18m6-14H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"],
                ["facturas", "Facturação", "M6 2h9l5 5v15H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm8 0v6h6M8 13h8m-8 4h8"],
            ],
        },
        {
            title: "CADASTROS",
            items: [
                ["clientes", "Clientes", "M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2m6-10a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm10 10v-2a4 4 0 0 0-3-3.9m1-12a4 4 0 0 1 0 7.8"],
                ["ligacoes", "Ligações", "M3 7h18M5 7l2-4h10l2 4m-14 0 2 14h10l2-14M9 11v6m6-6v6"],
                ["contadores", "Contadores", "M12 2v3m0 14v3M4.93 4.93l2.12 2.12m9.9 9.9 2.12 2.12M2 12h3m14 0h3m-17.07 7.07 2.12-2.12m9.9-9.9 2.12-2.12M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Z"],
                ["tarifas", "Tarifas", "M12 2v20m5-15a4 4 0 0 0-4-2H9a4 4 0 0 0 0 8h6a4 4 0 0 1 0 8h-4a4 4 0 0 1-4-2"],
            ],
        },
    ],
    admin: [
        {
            title: "ADMINISTRAÇÃO",
            items: [
                ["utilizadores", "Utilizadores", "M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2m6-10a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm10 10v-2a4 4 0 0 0-3-3.9m1-12a4 4 0 0 1 0 7.8"],
            ],
        },
    ],
    client: [
        {
            title: "A MINHA CONTA",
            items: [
                ["portal", "Resumo", "M4 13h6V3H4v10Zm10 8h6V11h-6v10ZM4 21h6v-4H4v4Zm10-14h6V3h-6v4Z"],
                ["portal-ligacoes", "As minhas ligações", "M3 7h18M5 7l2-4h10l2 4m-14 0 2 14h10l2-14M9 11v6m6-6v6"],
                ["portal-leituras", "As minhas leituras", "M12 3v18m6-14H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"],
                ["portal-facturas", "As minhas facturas", "M6 2h9l5 5v15H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm8 0v6h6M8 13h8m-8 4h8"],
            ],
        },
    ],
};

function h(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function initials(name) {
    return String(name || "pfunaMati")
        .trim()
        .split(/\s+/)
        .slice(0, 2)
        .map((part) => part[0] || "")
        .join("")
        .toLocaleUpperCase("pt");
}

function formatDate(value, options = {}) {
    if (!value) return "—";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "—";
    return new Intl.DateTimeFormat("pt-MZ", {
        day: "2-digit",
        month: "short",
        year: options.year === false ? undefined : "numeric",
        ...(options.time ? { hour: "2-digit", minute: "2-digit" } : {}),
    }).format(date);
}

function formatMoney(value) {
    return new Intl.NumberFormat("pt-MZ", {
        style: "currency",
        currency: "MZN",
        maximumFractionDigits: 2,
    }).format(Number(value || 0));
}

function formatVolume(value) {
    return `${new Intl.NumberFormat("pt-MZ", { maximumFractionDigits: 3 }).format(Number(value || 0))} m³`;
}

function roleTitle(roles) {
    return roles.map((role) => roleLabels[role] || role).join(", ");
}

function navigate(view) {
    state.view = view;
    state.page = 1;
    renderNavigation();
    renderCurrentView();
}

function renderNavigation() {
    const groups = state.user.perfis.includes("CLIENTE")
        ? navGroups.client
        : [...navGroups.staff, ...(state.user.perfis.includes("ADMIN") ? navGroups.admin : [])];

    navigation.replaceChildren();
    for (const group of groups) {
        const section = document.createElement("section");
        section.className = "navigation-group";
        const heading = document.createElement("span");
        heading.className = "navigation-heading";
        heading.textContent = group.title;
        section.append(heading);

        for (const [view, label, iconPath] of group.items) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = `nav-item${state.view === view ? " active" : ""}`;
            button.dataset.view = view;
            button.setAttribute("aria-current", state.view === view ? "page" : "false");
            button.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="${iconPath}"/></svg><span>${h(label)}</span>`;
            button.addEventListener("click", () => navigate(view));
            section.append(button);
        }
        navigation.append(section);
    }

    if (!state.user.perfis.includes("CLIENTE")) {
        const future = document.createElement("section");
        future.className = "navigation-group";
        const heading = document.createElement("span");
        heading.className = "navigation-heading";
        heading.textContent = "EM DESENVOLVIMENTO";
        future.append(heading);
        for (const [label, path] of [
            ["Pagamentos", "M3 6h18v13H3zM3 10h18m-14 5h4"],
        ]) {
            const item = document.createElement("span");
            item.className = "nav-item disabled";
            item.setAttribute("aria-disabled", "true");
            item.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="${path}"/></svg><span>${label}</span><small class="nav-coming">BREVEMENTE</small>`;
            future.append(item);
        }
        navigation.append(future);
    }
}

function setHeader(title) {
    document.querySelector("#current-section").textContent = title;
    document.querySelector("#account-name").textContent = state.user.nome;
    document.querySelector("#account-role").textContent = roleTitle(state.user.perfis);
    document.querySelector("#account-avatar").textContent = initials(state.user.nome);
}

function pageHead(kicker, title, description, action = "") {
    return `<header class="page-head"><div><span class="section-kicker">${h(kicker)}</span><h1>${h(title)}</h1><p>${h(description)}</p></div>${action}</header>`;
}

function metricCard(label, value, note, iconPath, tone = "") {
    return `<article class="metric-card"><span class="metric-icon ${tone}"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="${iconPath}"/></svg></span><span class="metric-copy"><span class="metric-label">${h(label)}</span><strong class="metric-value">${h(value)}</strong><span class="metric-note">${h(note)}</span></span><i class="metric-spark"></i></article>`;
}

function tableMarkup(headers, rows, emptyMessage) {
    const headerMarkup = headers.map((header) => `<th scope="col">${h(header)}</th>`).join("");
    const rowMarkup = rows.length
        ? rows.map((row) => (typeof row === "object" && row !== null && row.html ? row.html : String(row))).join("")
        : `<tr><td class="table-empty" colspan="${headers.length}">${h(emptyMessage)}</td></tr>`;
    return `<div class="table-scroll"><table><thead><tr>${headerMarkup}</tr></thead><tbody>${rowMarkup}</tbody></table></div>`;
}

function dataTablePanel({ title, description, headers, rows, empty, searchPlaceholder = "Pesquisar...", search = true }) {
    return `<section class="panel table-panel">
        <div class="panel-head"><div><h2>${h(title)}</h2><p>${h(description)}</p></div></div>
        ${search ? `<div class="table-toolbar"><label class="search-field"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg><span class="visually-hidden">${h(searchPlaceholder)}</span><input id="table-search" type="search" placeholder="${h(searchPlaceholder)}" autocomplete="off"></label></div>` : ""}
        <div id="table-content">${tableMarkup(headers, rows, empty)}</div>
        <div class="table-footer"><span id="table-caption">—</span><div class="pagination"><button id="page-previous" type="button" aria-label="Página anterior" disabled><svg viewBox="0 0 24 24"><path d="m15 18-6-6 6-6"/></svg></button><span id="page-current">1</span><button id="page-next" type="button" aria-label="Página seguinte" disabled><svg viewBox="0 0 24 24"><path d="m9 18 6-6-6-6"/></svg></button></div></div>
    </section>`;
}

function attachTable(rows, emptyMessage, search = true) {
    let filtered = rows;
    let page = 1;
    const content = document.querySelector("#table-content");
    const headers = [...content.querySelectorAll("thead th")].map((header) => header.textContent);
    const caption = document.querySelector("#table-caption");
    const pageCurrent = document.querySelector("#page-current");
    const previous = document.querySelector("#page-previous");
    const next = document.querySelector("#page-next");

    const render = () => {
        const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
        page = Math.min(page, pageCount);
        const start = (page - 1) * PAGE_SIZE;
        const currentSlice = filtered.slice(start, start + PAGE_SIZE).map((r) => (r && r.html ? r.html : r));
        content.innerHTML = tableMarkup(headers, currentSlice, emptyMessage);
        caption.textContent = filtered.length === 0
            ? "0 registos"
            : `A mostrar ${start + 1}–${Math.min(start + PAGE_SIZE, filtered.length)} de ${filtered.length} registos`;
        pageCurrent.textContent = String(page);
        previous.disabled = page <= 1;
        next.disabled = page >= pageCount;
    };

    previous.addEventListener("click", () => {
        if (page > 1) {
            page -= 1;
            render();
        }
    });
    next.addEventListener("click", () => {
        if (page < Math.ceil(filtered.length / PAGE_SIZE)) {
            page += 1;
            render();
        }
    });
    const searchInput = document.querySelector("#table-search");
    if (search && searchInput) {
        searchInput.addEventListener("input", () => {
            const term = normalize(searchInput.value.trim());
            filtered = term
                ? rows.filter((row) => normalize(row.text || row.html || String(row)).includes(term))
                : rows;
            page = 1;
            render();
        });
    }
    render();
}

function normalize(value) {
    return String(value || "").normalize("NFD").replace(/\p{Diacritic}/gu, "").toLocaleLowerCase("pt");
}

function showToast(message, isError = false) {
    const toast = document.createElement("div");
    toast.className = `toast${isError ? " error" : ""}`;
    toast.textContent = message;
    const container = document.querySelector("#toast-container");
    container.append(toast);
    window.setTimeout(() => toast.remove(), 4000);
}

function showError(error) {
    if (error instanceof ApiError && error.status === 401) {
        window.location.replace("/login.html");
        return;
    }
    workspace.innerHTML = `<div class="error-state"><strong>Não foi possível carregar esta área.</strong><p>${h(error.message)}</p><button class="button button-muted" id="retry-view" type="button">Tentar novamente</button></div>`;
    document.querySelector("#retry-view").addEventListener("click", renderCurrentView);
}

async function loadView(loader) {
    workspace.innerHTML = `<div class="loading-state"><span class="spinner"></span><span>A carregar informação...</span></div>`;
    try {
        await loader();
    } catch (error) {
        showError(error);
    }
}

async function renderCurrentView() {
    const viewTitles = {
        dashboard: "Visão geral",
        clientes: "Clientes",
        ligacoes: "Ligações",
        leituras: "Leituras",
        contadores: "Contadores",
        tarifas: "Tarifas",
        utilizadores: "Utilizadores",
        facturas: "Facturação",
        portal: "A minha conta",
        "portal-ligacoes": "As minhas ligações",
        "portal-leituras": "As minhas leituras",
        "portal-facturas": "As minhas facturas",
    };
    setHeader(viewTitles[state.view] || "Visão geral");

    if (state.user.perfis.includes("CLIENTE")) {
        await loadView(() => renderPortal(state.view));
        return;
    }

    const views = {
        dashboard: renderDashboard,
        clientes: renderClients,
        ligacoes: renderConnections,
        leituras: renderReadings,
        contadores: renderMeters,
        tarifas: renderTariffs,
        utilizadores: renderUsers,
        facturas: renderInvoices,
    };
    const render = views[state.view] || renderDashboard;
    await loadView(render);
}

async function renderDashboard() {
    const [summary, readings] = await Promise.all([
        api.get("/dashboard/resumo"),
        api.get("/leituras"),
    ]);

    const recent = readings.slice(0, 6);
    const quickActions = state.user.perfis.includes("ADMIN")
        ? `<button class="quick-action" data-action="new-client"><span>＋</span><strong>Registar cliente</strong><span>›</span></button>`
        : "";
    const readingAction = state.user.perfis.some((role) => ["ADMIN", "LEITOR"].includes(role))
        ? `<button class="quick-action" data-action="new-reading"><span>↗</span><strong>Registar leitura</strong><span>›</span></button>`
        : "";
    const invoiceAction = state.user.perfis.some((role) => ["ADMIN", "LEITOR"].includes(role))
        ? `<button class="quick-action" data-view="facturas"><span>📄</span><strong>Gerir facturação</strong><span>›</span></button>`
        : "";

    workspace.innerHTML = `
        ${pageHead("PAINEL DE OPERAÇÕES", `Bom dia, ${state.user.nome.split(" ")[0]}`, "Acompanhe os principais indicadores e a actividade recente do serviço.")}
        <div class="metrics-grid">
            ${metricCard("Clientes registados", summary.totalClientes, "cadastros no sistema", "M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2m6-10a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm10 10v-2a4 4 0 0 0-3-3.9m1-12a4 4 0 0 1 0 7.8")}
            ${metricCard("Ligações activas", summary.ligacoesActivas, "pontos de abastecimento", "M3 7h18M5 7l2-4h10l2 4m-14 0 2 14h10l2-14M9 11v6m6-6v6", "teal")}
            ${metricCard("Contadores activos", summary.contadoresActivos, "equipamentos instalados", "M12 2v3m0 14v3M4.93 4.93l2.12 2.12m9.9 9.9 2.12 2.12M2 12h3m14 0h3m-17.07 7.07 2.12-2.12m9.9-9.9 2.12-2.12M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Z", "blue")}
            ${metricCard("Volume registado no mês", formatVolume(summary.volumeEsteMesM3), `${summary.leiturasEsteMes} leituras registadas`, "M12 3C9 7.5 5 11 5 15.2a7 7 0 0 0 14 0C19 11 15 7.5 12 3Z", "gold")}
        </div>
        <div class="content-grid">
            <section class="panel">
                <div class="panel-head"><div><h2>Leituras recentes</h2><p>Últimas leituras registadas no sistema</p></div><button class="button-link" data-view="leituras">Ver todas <span aria-hidden="true">→</span></button></div>
                ${tableMarkup(["CLIENTE / LIGAÇÃO", "CONTADOR", "DATA", "LEITURA", "TIPO"], recent.map((reading) => `<tr><td><span class="table-primary">${h(reading.cliente)}</span><span class="table-secondary">${h(reading.codigoLigacao)}</span></td><td>${h(reading.numeroSerie)}</td><td>${h(formatDate(reading.dataLeitura, { time: true }))}</td><td><span class="table-primary">${h(formatVolume(reading.valorM3))}</span></td><td><span class="badge ${reading.tipo === "ESTIMADA" ? "warning" : ""}">${h(reading.tipo === "ESTIMADA" ? "Estimada" : "Real")}</span></td></tr>`), "Ainda não existem leituras registadas.")}
            </section>
            <section class="panel">
                <div class="panel-head"><div><h2>Acesso rápido</h2><p>Acções disponíveis para o seu perfil</p></div></div>
                <div class="panel-body"><div class="quick-actions">${quickActions}${readingAction}${invoiceAction}<button class="quick-action" data-view="clientes"><span>◎</span><strong>Consultar clientes</strong><span>›</span></button></div>
                    <div class="notice-card" style="margin-top:13px"><span class="notice-icon">i</span><div><strong>Visão operacional</strong><p>O volume mensal é calculado pela diferença entre leituras acumuladas do mesmo contador.</p></div></div>
                </div>
            </section>
        </div>`;
    workspace.querySelectorAll("[data-view]").forEach((button) => button.addEventListener("click", () => navigate(button.dataset.view)));
    workspace.querySelectorAll("[data-action]").forEach((button) => button.addEventListener("click", () => {
        if (button.dataset.action === "new-client") openClientForm();
        if (button.dataset.action === "new-reading") openReadingForm();
    }));
}

function clientRows(clients) {
    return clients.map((client) => ({
        text: `${client.nome} ${client.documento} ${client.email} ${client.telefone || ""}`,
        html: `<tr><td><span class="table-primary">${h(client.nome)}</span><span class="table-secondary">${h(client.email)}</span></td><td>${h(client.documento)}</td><td>${h(client.telefone || "—")}</td><td>${h(formatDate(client.dataRegisto))}</td><td><span class="badge">Registado</span></td></tr>`,
    }));
}

async function renderClients() {
    const clients = await api.get("/clientes");
    state.clients = clients;
    const canCreate = state.user.perfis.includes("ADMIN");
    const action = canCreate
        ? `<button class="button button-primary" id="create-client"><svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg> Novo cliente</button>`
        : "";
    const rows = clientRows(clients);
    workspace.innerHTML = `${pageHead("CADASTRO DE CLIENTES", "Clientes", "Consulte os registos de clientes e mantenha os dados de contacto actualizados.", action)}
        ${dataTablePanel({ title: "Registo de clientes", description: `${clients.length} clientes disponíveis para consulta`, headers: ["CLIENTE", "DOCUMENTO", "TELEFONE", "REGISTADO EM", "ESTADO"], rows: rows.map((row) => row.html), empty: "Não existem clientes registados.", searchPlaceholder: "Pesquisar por nome, documento ou contacto..." })}`;
    attachTable(rows, "Não existem clientes registados.");
    document.querySelector("#create-client")?.addEventListener("click", openClientForm);
}

function openClientForm() {
    openDialog("Novo cliente", [
        field("Nome completo", "nome", { required: true, maxLength: 120, wide: true, placeholder: "Ex.: Ana Maria Mucavele" }),
        field("Documento de identificação", "documento", { required: true, maxLength: 40, placeholder: "Número do documento" }),
        field("Telefone", "telefone", { maxLength: 30, placeholder: "+258 84 000 0000" }),
        field("Endereço de email", "email", { type: "email", required: true, maxLength: 160, wide: true, placeholder: "nome@exemplo.com" }),
    ], async (data) => {
        await api.post("/clientes", {
            nome: data.nome.trim(),
            documento: data.documento.trim(),
            email: data.email.trim(),
            telefone: data.telefone.trim() || null,
        });
        showToast("Cliente registado com sucesso.");
        await renderCurrentView();
    });
}

async function renderConnections() {
    const rowsData = await api.get("/ligacoes");
    const rows = rowsData.map((item) => ({
        text: `${item.codigo} ${item.cliente} ${item.endereco} ${item.bairro} ${item.cidade} ${item.estado}`,
        html: `<tr><td><span class="table-primary">${h(item.codigo)}</span><span class="table-secondary">${h(item.cliente)}</span></td><td>${h(item.endereco)}</td><td>${h(item.bairro)}</td><td>${h(item.cidade)}${item.provincia ? `<span class="table-secondary">${h(item.provincia)}</span>` : ""}</td><td>${h(formatDate(item.dataActivacao))}</td><td><span class="badge">${h(item.estado)}</span></td></tr>`,
    }));
    workspace.innerHTML = `${pageHead("REDE DE ABASTECIMENTO", "Ligações", "Pontos de fornecimento associados a clientes e endereços.", `<span class="badge neutral">Consulta</span>`)}
        ${dataTablePanel({ title: "Ligações de água", description: `${rows.length} registos encontrados`, headers: ["CÓDIGO / CLIENTE", "ENDEREÇO", "BAIRRO", "CIDADE / PROVÍNCIA", "ACTIVADA EM", "ESTADO"], rows: rows.map((row) => row.html), empty: "Ainda não existem ligações registadas.", searchPlaceholder: "Pesquisar código, cliente ou bairro..." })}`;
    attachTable(rows, "Ainda não existem ligações registadas.");
}

async function renderMeters() {
    const meters = await api.get("/contadores");
    state.meters = meters;
    const rows = meters.map((meter) => ({
        text: `${meter.numeroSerie} ${meter.modelo} ${meter.codigoLigacao} ${meter.cliente} ${meter.estado}`,
        html: `<tr><td><span class="table-primary">${h(meter.numeroSerie)}</span><span class="table-secondary">${h(meter.modelo || "Modelo não indicado")}</span></td><td>${h(meter.codigoLigacao)}</td><td>${h(meter.cliente)}</td><td>${h(formatDate(meter.dataInstalacao))}</td><td><span class="badge ${meter.estado === "ACTIVO" ? "" : "warning"}">${h(meter.estado)}</span></td></tr>`,
    }));
    workspace.innerHTML = `${pageHead("EQUIPAMENTO", "Contadores", "Equipamentos associados às ligações de água e utilizados no registo de leituras.")}
        ${dataTablePanel({ title: "Contadores instalados", description: `${meters.length} equipamentos encontrados`, headers: ["NÚMERO DE SÉRIE / MODELO", "LIGAÇÃO", "CLIENTE", "INSTALADO EM", "ESTADO"], rows: rows.map((row) => row.html), empty: "Ainda não existem contadores registados.", searchPlaceholder: "Pesquisar série, ligação ou cliente..." })}`;
    attachTable(rows, "Ainda não existem contadores registados.");
}

async function renderReadings() {
    const [readings, meters] = await Promise.all([api.get("/leituras"), api.get("/contadores")]);
    state.meters = meters;
    const action = `<button class="button button-primary" id="create-reading"><svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg> Registar leitura</button>`;
    const rows = readings.map((reading) => ({
        text: `${reading.cliente} ${reading.codigoLigacao} ${reading.numeroSerie} ${reading.leitor || ""} ${reading.tipo}`,
        html: `<tr><td><span class="table-primary">${h(reading.cliente)}</span><span class="table-secondary">${h(reading.codigoLigacao)}</span></td><td>${h(reading.numeroSerie)}</td><td>${h(formatDate(reading.dataLeitura, { time: true }))}</td><td><span class="table-primary">${h(formatVolume(reading.valorM3))}</span></td><td><span class="badge ${reading.tipo === "ESTIMADA" ? "warning" : ""}">${h(reading.tipo === "ESTIMADA" ? "Estimada" : "Real")}</span></td><td>${h(reading.leitor || "—")}</td></tr>`,
    }));
    workspace.innerHTML = `${pageHead("LEITURAS DE CONTADORES", "Leituras", "Registe leituras acumuladas e acompanhe os valores por contador.", action)}
        ${dataTablePanel({ title: "Histórico de leituras", description: `A apresentar as ${Math.min(readings.length, 50)} leituras mais recentes`, headers: ["CLIENTE / LIGAÇÃO", "CONTADOR", "DATA E HORA", "LEITURA", "TIPO", "REGISTADA POR"], rows: rows.map((row) => row.html), empty: "Ainda não existem leituras registadas.", searchPlaceholder: "Pesquisar cliente, contador ou ligação..." })}`;
    attachTable(rows, "Ainda não existem leituras registadas.");
    document.querySelector("#create-reading").addEventListener("click", openReadingForm);
}

function openReadingForm() {
    const activeMeters = state.meters.filter((meter) => meter.estado === "ACTIVO");
    if (!activeMeters.length) {
        showToast("Não existem contadores activos para registar uma leitura.", true);
        return;
    }
    const meterOptions = activeMeters.map((meter) => ({
        value: meter.id,
        label: `${meter.numeroSerie} · ${meter.codigoLigacao} · ${meter.cliente}`,
    }));
    const now = new Date();
    const localNow = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
    openDialog("Registar leitura", [
        field("Contador activo", "contadorId", { type: "select", required: true, wide: true, options: meterOptions }),
        field("Data e hora da leitura", "dataLeitura", { type: "datetime-local", required: true, value: localNow }),
        field("Leitura acumulada (m³)", "valorM3", { type: "number", required: true, min: 0, step: "0.001", placeholder: "Ex.: 125.750" }),
        field("Tipo de leitura", "tipo", { type: "select", required: true, value: "REAL", options: [{ value: "REAL", label: "Real" }, { value: "ESTIMADA", label: "Estimada" }] }),
        field("Observação", "observacao", { type: "textarea", maxLength: 500, wide: true, placeholder: "Observação opcional" }),
    ], async (data) => {
        await api.post("/leituras", {
            contadorId: data.contadorId,
            dataLeitura: data.dataLeitura,
            valorM3: Number(data.valorM3),
            tipo: data.tipo,
            observacao: data.observacao.trim() || null,
        });
        showToast("Leitura registada com sucesso.");
        await renderCurrentView();
    });
}

async function renderTariffs() {
    const tariffs = await api.get("/tarifas");
    const rows = tariffs.map((tariff) => ({
        text: `${tariff.nome} ${tariff.descricao || ""} ${tariff.activa ? "activa" : "inactiva"}`,
        html: `<tr><td><span class="table-primary">${h(tariff.nome)}</span><span class="table-secondary">${h(tariff.descricao || "—")}</span></td><td>${h(formatMoney(tariff.taxaFixa))}</td><td>${h(formatMoney(tariff.precoPorM3))}</td><td>${h(formatDate(tariff.vigenteDesde))}</td><td><span class="badge ${tariff.activa ? "" : "neutral"}">${tariff.activa ? "Activa" : "Inactiva"}</span></td></tr>`,
    }));
    workspace.innerHTML = `${pageHead("TABELA DE PREÇOS", "Tarifas", "Consulte as tarifas configuradas e os respectivos valores em meticais.")}
        ${dataTablePanel({ title: "Tarifas do serviço", description: `${tariffs.length} tarifas configuradas`, headers: ["DESIGNAÇÃO", "TAXA FIXA", "PREÇO POR m³", "VIGENTE DESDE", "ESTADO"], rows: rows.map((row) => row.html), empty: "Não existem tarifas configuradas.", searchPlaceholder: "Pesquisar tarifa..." })}`;
    attachTable(rows, "Não existem tarifas configuradas.");
}

async function renderUsers() {
    const [users, clients] = await Promise.all([api.get("/admin/utilizadores"), api.get("/clientes")]);
    state.clients = clients;
    const action = `<button class="button button-primary" id="create-user"><svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg> Criar utilizador</button>`;
    const rows = users.map((user) => ({
        text: `${user.nome} ${user.nomeUtilizador} ${user.email} ${user.perfis.join(" ")}`,
        html: `<tr><td><span class="table-primary">${h(user.nome)}</span><span class="table-secondary">${h(user.email)}</span></td><td>${h(user.nomeUtilizador)}</td><td>${user.perfis.map((role) => `<span class="badge ${role.toLowerCase()}">${h(roleLabels[role] || role)}</span>`).join(" ")}</td><td><span class="badge ${user.activo ? "" : "neutral"}">${user.activo ? "Activo" : "Inactivo"}</span></td></tr>`,
    }));
    workspace.innerHTML = `${pageHead("CONTROLO DE ACESSO", "Utilizadores", "Gira contas de acesso e atribua apenas os perfis necessários.", action)}
        ${dataTablePanel({ title: "Contas do sistema", description: `${users.length} contas registadas`, headers: ["UTILIZADOR", "NOME DE ACESSO", "PERFIS ATRIBUÍDOS", "ESTADO"], rows: rows.map((row) => row.html), empty: "Não existem utilizadores registados.", searchPlaceholder: "Pesquisar utilizador..." })}
        <div class="notice-card" style="margin-top:12px"><span class="notice-icon">i</span><div><strong>Palavras-passe protegidas</strong><p>As novas palavras-passe são armazenadas com hash BCrypt e nunca são devolvidas pela API.</p></div></div>`;
    attachTable(rows, "Não existem utilizadores registados.");
    document.querySelector("#create-user").addEventListener("click", () => openUserForm(clients));
}

async function renderInvoices() {
    const [invoices, summary, meters, tariffs] = await Promise.all([
        api.get("/facturas"),
        api.get("/facturas/resumo"),
        api.get("/contadores"),
        api.get("/tarifas"),
    ]);
    state.meters = meters;
    const canCreate = !state.user.perfis.includes("CLIENTE");
    const isAdmin = state.user.perfis.includes("ADMIN");

    const action = canCreate
        ? `<button class="button button-primary" id="create-invoice"><svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg> Emitir factura</button>`
        : "";

    const rows = invoices.map((inv) => ({
        text: `${inv.numero} ${inv.clienteNome} ${inv.clienteDocumento} ${inv.ligacaoCodigo} ${inv.contadorNumeroSerie} ${inv.estado}`,
        html: `<tr>
            <td><span class="table-primary">${h(inv.numero)}</span><span class="table-secondary">${h(formatDate(inv.dataEmissao))}</span></td>
            <td><span class="table-primary">${h(inv.clienteNome)}</span><span class="table-secondary">${h(inv.clienteDocumento)}</span></td>
            <td>${h(inv.ligacaoCodigo)}</td>
            <td>${h(inv.contadorNumeroSerie)}</td>
            <td>${h(formatDate(inv.periodoInicio, { year: false }))} — ${h(formatDate(inv.periodoFim))}</td>
            <td><span class="table-primary">${h(formatVolume(inv.volumeM3))}</span></td>
            <td><span class="table-primary">${h(formatMoney(inv.valorTotal))}</span><span class="table-secondary">Fixa: ${h(formatMoney(inv.taxaFixa))}</span></td>
            <td><span class="badge ${inv.estado === "PAGA" ? "" : inv.estado === "PENDENTE" ? "warning" : "neutral"}">${h(inv.estado)}</span></td>
            <td>
                ${isAdmin && inv.estado === "PENDENTE" ? `<button class="button-xs pay-invoice" data-id="${inv.id}" type="button">Liquidar</button>` : ""}
                ${canCreate && inv.estado === "PENDENTE" ? `<button class="button-xs remind-invoice" data-id="${inv.id}" type="button" style="margin-left:4px; border-color:#d49b28; color:#9c6e11; background:#fef8ea;">Lembrete</button>` : ""}
                ${inv.estado === "PAGA" ? "—" : ""}
            </td>
        </tr>`,
    }));

    workspace.innerHTML = `${pageHead("GESTÃO DE FACTURAÇÃO", "Facturação", "Consulte e emita facturas de consumo de água aos clientes.", action)}
        <div class="metrics-grid">
            ${metricCard("Total facturado", formatMoney(summary.totalFacturado), `${summary.totalFacturas} facturas emitidas`, "M12 2v20m5-15a4 4 0 0 0-4-2H9a4 4 0 0 0 0 8h6a4 4 0 0 1 0 8h-4a4 4 0 0 1-4-2", "teal")}
            ${metricCard("Facturas pendentes", formatMoney(summary.totalPendente), `${summary.facturasPendentes} a aguardar liquidação`, "M12 8v4l3 3m6-3a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z", "amber")}
            ${metricCard("Facturas liquidadas", formatMoney(summary.totalPago), `${summary.facturasPagas} valores confirmados`, "M9 12l2 2 4-4m6 2a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z")}
            ${metricCard("Volume medido", formatVolume(summary.volumeTotalM3), "m³ facturados globalmente", "M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z", "blue")}
        </div>
        ${dataTablePanel({
            title: "Histórico de facturas",
            description: `${invoices.length} facturas registadas no sistema`,
            headers: ["NÚMERO / DATA", "CLIENTE", "LIGAÇÃO", "CONTADOR", "PERÍODO", "CONSUMO", "TOTAL", "ESTADO", "ACÇÕES"],
            rows: rows.map((r) => r.html),
            empty: "Não existem facturas registadas no sistema.",
            searchPlaceholder: "Pesquisar por número, cliente, documento ou ligação...",
        })}`;

    attachTable(rows, "Não existem facturas registadas no sistema.");

    if (canCreate) {
        document.querySelector("#create-invoice")?.addEventListener("click", () => openInvoiceForm(meters, tariffs));
    }

    workspace.querySelectorAll(".pay-invoice").forEach((button) => {
        button.addEventListener("click", async () => {
            if (!confirm("Confirmar a liquidação do pagamento desta factura?")) return;
            try {
                await api.post(`/facturas/${button.dataset.id}/liquidar`);
                showToast("Factura liquidada com sucesso.");
                await renderCurrentView();
            } catch (error) {
                showToast(error.message || "Erro ao liquidar factura.", true);
            }
        });
    });

    workspace.querySelectorAll(".remind-invoice").forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                const res = await api.post(`/facturas/${button.dataset.id}/lembrete`);
                showToast(res.mensagem || "Lembrete enviado com sucesso.");
            } catch (error) {
                showToast(error.message || "Erro ao enviar lembrete.", true);
            }
        });
    });
}

function openInvoiceForm(meters, tariffs) {
    const activeMeters = meters.filter((m) => m.estado === "ACTIVO");
    if (!activeMeters.length) {
        showToast("Não existem contadores activos disponíveis.", true);
        return;
    }
    const meterOptions = activeMeters.map((m) => ({
        value: m.id,
        label: `${m.numeroSerie} · ${m.codigoLigacao} (${m.cliente})`,
    }));

    const activeTariffs = tariffs.filter((t) => t.activa);
    const tariffOptions = activeTariffs.map((t) => ({
        value: t.id,
        label: `${t.nome} (Fixa: ${formatMoney(t.taxaFixa)} · ${formatMoney(t.precoPorM3)}/m³)`,
    }));

    const today = new Date().toISOString().slice(0, 10);
    const dueDate = new Date(Date.now() + 15 * 86400000).toISOString().slice(0, 10);

    openDialog("Emitir nova factura", [
        field("Contador / Ligação", "contadorId", { type: "select", required: true, wide: true, options: meterOptions }),
        field("Tarifa aplicável", "tarifaId", { type: "select", required: true, wide: true, options: tariffOptions }),
        field("Leitura inicial (m³)", "leituraInicialM3", { type: "number", step: "0.001", min: 0, placeholder: "Opcional (calculada automaticamente se vazia)" }),
        field("Leitura final (m³)", "leituraFinalM3", { type: "number", step: "0.001", min: 0, placeholder: "Opcional (última medição se vazia)" }),
        field("Data de emissão", "dataEmissao", { type: "date", required: true, value: today }),
        field("Data de vencimento", "dataVencimento", { type: "date", required: true, value: dueDate }),
    ], async (data) => {
        if (!data.contadorId) {
            throw new Error("Seleccione o contador para emitir a factura.");
        }
        await api.post("/facturas/gerar", {
            contadorId: data.contadorId,
            tarifaId: data.tarifaId || null,
            leituraInicialM3: data.leituraInicialM3 ? Number(data.leituraInicialM3) : null,
            leituraFinalM3: data.leituraFinalM3 ? Number(data.leituraFinalM3) : null,
            dataEmissao: data.dataEmissao || null,
            dataVencimento: data.dataVencimento || null,
        });
        showToast("Factura emitida com sucesso.");
        await renderCurrentView();
    });
}

async function renderPortal(view) {
    const [clients, readings, invoices, alerts] = await Promise.all([
        api.get("/portal/clientes"),
        api.get("/portal/leituras"),
        api.get("/portal/facturas"),
        api.get("/portal/alertas"),
    ]);
    const connections = clients.flatMap((client) => client.ligacoes.map((connection) => ({ ...connection, cliente: client.nome })));

    const alertBanner = alerts && alerts.length > 0
        ? `<div class="notice-card" style="margin-bottom:15px; border-left:4px solid var(--amber); background:#fffcf4;">
            <span class="notice-icon" style="color:var(--amber); background:#fef4dc;">⚠️</span>
            <div>
                <strong style="color:#785800;">Avisos de Vencimento de Facturas</strong>
                ${alerts.map((a) => `<p style="margin-top:4px; color:#6d511a;">${h(a.mensagem)}</p>`).join("")}
            </div>
        </div>`
        : "";

    if (view === "portal-ligacoes") {
        const rows = connections.map((item) => ({
            text: `${item.codigo} ${item.endereco} ${item.bairro} ${item.cidade} ${item.estado}`,
            html: `<tr><td><span class="table-primary">${h(item.codigo)}</span></td><td>${h(item.endereco)}</td><td>${h(item.bairro)}</td><td>${h(item.cidade)}</td><td>${h(formatDate(item.dataActivacao))}</td><td><span class="badge">${h(item.estado)}</span></td></tr>`,
        }));
        workspace.innerHTML = `${pageHead("ÁREA DO CLIENTE", "As minhas ligações", "Consulte os pontos de abastecimento associados à sua conta.")}${dataTablePanel({ title: "Ligações associadas", description: `${connections.length} ligações disponíveis`, headers: ["CÓDIGO", "ENDEREÇO", "BAIRRO", "CIDADE", "ACTIVADA EM", "ESTADO"], rows: rows.map((row) => row.html), empty: "Não existem ligações associadas à sua conta.", searchPlaceholder: "Pesquisar ligação ou endereço..." })}`;
        attachTable(rows, "Não existem ligações associadas à sua conta.");
        return;
    }

    if (view === "portal-leituras") {
        const rows = readings.map((reading) => ({
            text: `${reading.codigoLigacao} ${reading.numeroSerie} ${reading.dataLeitura} ${reading.tipo}`,
            html: `<tr><td><span class="table-primary">${h(reading.codigoLigacao)}</span></td><td>${h(reading.numeroSerie)}</td><td>${h(formatDate(reading.dataLeitura, { time: true }))}</td><td>${h(formatVolume(reading.valorM3))}</td><td><span class="badge ${reading.tipo === "ESTIMADA" ? "warning" : ""}">${h(reading.tipo)}</span></td></tr>`,
        }));
        workspace.innerHTML = `${pageHead("ÁREA DO CLIENTE", "As minhas leituras", "Histórico de leituras dos seus contadores.")}
            ${dataTablePanel({ title: "Leituras da conta", description: `Até ${Math.min(readings.length, 50)} registos recentes`, headers: ["LIGAÇÃO", "CONTADOR", "DATA E HORA", "LEITURA", "TIPO"], rows: rows.map((row) => row.html), empty: "Ainda não existem leituras para as suas ligações.", searchPlaceholder: "Pesquisar ligação ou contador..." })}`;
        attachTable(rows, "Ainda não existem leituras para as suas ligações.");
        return;
    }

    if (view === "portal-facturas") {
        const rows = invoices.map((inv) => ({
            text: `${inv.numero} ${inv.ligacaoCodigo} ${inv.contadorNumeroSerie} ${inv.estado}`,
            html: `<tr>
                <td><span class="table-primary">${h(inv.numero)}</span><span class="table-secondary">${h(formatDate(inv.dataEmissao))}</span></td>
                <td>${h(inv.ligacaoCodigo)}</td>
                <td>${h(inv.contadorNumeroSerie)}</td>
                <td>${h(formatDate(inv.periodoInicio, { year: false }))} — ${h(formatDate(inv.periodoFim))}</td>
                <td>${h(formatVolume(inv.volumeM3))}</td>
                <td>${h(formatMoney(inv.taxaFixa))}</td>
                <td><strong>${h(formatMoney(inv.valorTotal))}</strong></td>
                <td>${h(formatDate(inv.dataVencimento))}</td>
                <td><span class="badge ${inv.estado === "PAGA" ? "" : "warning"}">${h(inv.estado)}</span></td>
            </tr>`,
        }));
        workspace.innerHTML = `${pageHead("ÁREA DO CLIENTE", "As minhas facturas", "Histórico e estado das suas facturas de consumo de água.")}
            ${alertBanner}
            ${dataTablePanel({
                title: "Facturas da conta",
                description: `${invoices.length} facturas registadas`,
                headers: ["NÚMERO", "LIGAÇÃO", "CONTADOR", "PERÍODO", "CONSUMO", "TAXA FIXA", "TOTAL", "VENCIMENTO", "ESTADO"],
                rows: rows.map((r) => r.html),
                empty: "Ainda não existem facturas emitidas para as suas ligações.",
                searchPlaceholder: "Pesquisar por número ou ligação...",
            })}`;
        attachTable(rows, "Ainda não existem facturas emitidas para as suas ligações.");
        return;
    }

    const totalConnections = connections.length;
    const pendingInvoices = invoices.filter((i) => i.estado === "PENDENTE");
    const totalPendingAmount = pendingInvoices.reduce((acc, i) => acc + (Number(i.valorTotal) || 0), 0);
    const latest = readings.slice(0, 5);

    workspace.innerHTML = `${pageHead("ÁREA DO CLIENTE", `Olá, ${state.user.nome.split(" ")[0]}`, "Consulte a informação das ligações de água associadas à sua conta.")}
        ${alertBanner}
        <div class="metrics-grid">
            ${metricCard("Ligações associadas", totalConnections, "pontos de abastecimento", "M3 7h18M5 7l2-4h10l2 4m-14 0 2 14h10l2-14M9 11v6m6-6v6")}
            ${metricCard("Leituras recentes", readings.length, "registos disponíveis", "M12 3v18m6-14H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6", "teal")}
            ${metricCard("Facturas pendentes", pendingInvoices.length, "a regularizar", "M12 8v4l3 3m6-3a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z", pendingInvoices.length > 0 ? "amber" : "teal")}
            ${metricCard("Total a regularizar", formatMoney(totalPendingAmount), "valor em aberto", "M12 2v20m5-15a4 4 0 0 0-4-2H9a4 4 0 0 0 0 8h6a4 4 0 0 1 0 8h-4a4 4 0 0 1-4-2", pendingInvoices.length > 0 ? "gold" : "blue")}
        </div>
        <div class="content-grid">
            <section class="panel"><div class="panel-head"><div><h2>As minhas facturas recentes</h2><p>Estado e valores a pagamento</p></div><button class="button-link" data-view="portal-facturas">Ver todas →</button></div>
                <div class="panel-body activity-list">${invoices.slice(0, 5).map((item) => `<div class="activity-item"><span class="activity-marker">📄</span><span class="activity-copy"><strong>${h(item.numero)} · ${h(item.ligacaoCodigo)}</strong><span>Vence em ${h(formatDate(item.dataVencimento))}</span></span><span class="badge ${item.estado === "PAGA" ? "" : "warning"}">${h(item.estado)}</span></div>`).join("") || `<div class="empty-state">Ainda não existem facturas para as suas ligações.</div>`}</div>
            </section>
            <section class="panel"><div class="panel-head"><div><h2>Últimas leituras</h2><p>Actualizações dos seus contadores</p></div><button class="button-link" data-view="portal-leituras">Ver todas →</button></div>
                <div class="panel-body activity-list">${latest.map((item) => `<div class="activity-item"><span class="activity-marker">↗</span><span class="activity-copy"><strong>${h(item.codigoLigacao)} · ${h(item.numeroSerie)}</strong><span>${h(formatDate(item.dataLeitura, { time: true }))}</span></span><span class="activity-value">${h(formatVolume(item.valorM3))}</span></div>`).join("") || `<div class="empty-state">Ainda não há leituras registadas.</div>`}</div>
            </section>
        </div>`;
    workspace.querySelectorAll("[data-view]").forEach((button) => button.addEventListener("click", () => navigate(button.dataset.view)));
}

function field(label, name, options = {}) {
    const wide = options.wide ? " wide" : "";
    const required = options.required ? " required" : "";
    const maxLength = options.maxLength ? ` maxlength="${options.maxLength}"` : "";
    const minLength = options.minLength ? ` minlength="${options.minLength}"` : "";
    const hint = options.hint ? `<small class="field-hint">${h(options.hint)}</small>` : "";
    const placeholder = options.placeholder ? ` placeholder="${h(options.placeholder)}"` : "";
    let control;
    if (options.type === "select") {
        control = `<select name="${h(name)}"${required}>${options.options.map((option) => `<option value="${h(option.value)}"${option.value === options.value ? " selected" : ""}>${h(option.label)}</option>`).join("")}</select>`;
    } else if (options.type === "textarea") {
        control = `<textarea name="${h(name)}"${maxLength}${placeholder}></textarea>`;
    } else {
        const min = options.min !== undefined ? ` min="${options.min}"` : "";
        const step = options.step ? ` step="${options.step}"` : "";
        const value = options.value ? ` value="${h(options.value)}"` : "";
        control = `<input name="${h(name)}" type="${h(options.type || "text")}"${required}${maxLength}${minLength}${placeholder}${min}${step}${value}>`;
    }
    return `<label class="field${wide}"><span>${h(label)}${required ? ` <span aria-hidden="true">*</span>` : ""}</span>${control}${hint}</label>`;
}

function openDialog(title, fields, onSubmit) {
    document.querySelector("#dialog-kicker").textContent = "DADOS DO REGISTO";
    document.querySelector("#dialog-title").textContent = title;
    dialogFields.innerHTML = fields.join("");
    document.querySelector("#dialog-error").hidden = true;
    document.querySelector("#dialog-error").textContent = "";
    document.querySelector("#dialog-submit").textContent = "Guardar";
    dialogAction = onSubmit;
    dialogBusy = false;
    dialog.showModal();
    dialog.querySelector("input, select, textarea")?.focus();
}

document.querySelector("#dialog-close").addEventListener("click", () => dialog.close());
document.querySelector("#dialog-cancel").addEventListener("click", () => dialog.close());
dialog.addEventListener("click", (event) => {
    if (event.target === dialog) dialog.close();
});
dialogForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (dialogBusy || !dialogAction) return;
    dialogBusy = true;
    const submit = document.querySelector("#dialog-submit");
    const error = document.querySelector("#dialog-error");
    submit.disabled = true;
    try {
        const data = Object.fromEntries(new FormData(dialogForm).entries());
        await dialogAction(data);
        dialog.close();
    } catch (requestError) {
        error.textContent = requestError.message || "Não foi possível guardar o registo.";
        error.hidden = false;
    } finally {
        dialogBusy = false;
        submit.disabled = false;
    }
});

function openUserForm(clients) {
    const clientOptions = [
        { value: "", label: "Seleccione o cadastro do cliente" },
        ...clients.map((client) => ({ value: client.id, label: `${client.nome} · ${client.documento}` })),
    ];
    openDialog("Criar conta de utilizador", [
        field("Nome completo", "nome", { required: true, maxLength: 120, wide: true }),
        field("Nome de utilizador", "nomeUtilizador", { required: true, maxLength: 60, placeholder: "Ex.: maria.leitora" }),
        field("Email", "email", { type: "email", required: true, maxLength: 160 }),
        field("Palavra-passe temporária", "senha", { type: "password", required: true, minLength: 10, maxLength: 72, hint: "Use pelo menos 10 caracteres. O sistema guarda apenas um hash BCrypt." }),
        field("Perfil de acesso", "perfil", { type: "select", required: true, options: [
            { value: "LEITOR", label: "Leitor — regista e consulta leituras" },
            { value: "ADMIN", label: "Administrador — gere o sistema" },
            { value: "CLIENTE", label: "Cliente — consulta apenas os seus dados" },
        ] }),
        field("Cadastro associado", "clienteId", { type: "select", wide: true, options: clientOptions }),
    ], async (data) => {
        if (data.perfil === "CLIENTE" && !data.clienteId) {
            throw new Error("Seleccione o registo de cliente associado à conta.");
        }
        await api.post("/admin/utilizadores", {
            nome: data.nome.trim(),
            nomeUtilizador: data.nomeUtilizador.trim(),
            email: data.email.trim(),
            senha: data.senha,
            perfis: [data.perfil],
            clienteId: data.perfil === "CLIENTE" ? data.clienteId : null,
        });
        showToast("Conta criada com sucesso.");
        await renderCurrentView();
    });
    const roleSelect = dialogForm.elements.perfil;
    const clientSelect = dialogForm.elements.clienteId;
    const updateClientField = () => {
        const required = roleSelect.value === "CLIENTE";
        clientSelect.required = required;
        clientSelect.disabled = !required;
        clientSelect.closest(".field").style.opacity = required ? "1" : ".55";
    };
    roleSelect.addEventListener("change", updateClientField);
    updateClientField();
}

document.querySelector("#logout-button").addEventListener("click", async () => {
    try {
        await api.logout();
    } catch (error) {
        if (!(error instanceof ApiError) || error.status !== 401) {
            showToast(error.message || "Não foi possível terminar sessão.", true);
        }
    }
    window.location.replace("/login.html");
});

async function initialize() {
    try {
        state.user = await api.me();
    } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
            window.location.replace("/login.html");
            return;
        }
        workspace.innerHTML = `<div class="error-state">Não foi possível validar a sessão. Confirme se o backend e a base de dados estão disponíveis.</div>`;
        return;
    }

    renderNavigation();
    navigate(state.user.perfis.includes("CLIENTE") ? "portal" : "dashboard");
}

initialize();
