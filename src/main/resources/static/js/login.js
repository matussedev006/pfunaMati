import { api, ApiError } from "./api.js";

const form = document.querySelector("#login-form");
const errorBox = document.querySelector("#login-error");
const submitButton = document.querySelector("#login-submit");
const passwordInput = document.querySelector("#password");

document.querySelector("#current-year").textContent = String(new Date().getFullYear());

document.querySelector("#toggle-password").addEventListener("click", (event) => {
    const button = event.currentTarget;
    const show = passwordInput.type === "password";
    passwordInput.type = show ? "text" : "password";
    button.setAttribute("aria-label", show ? "Ocultar palavra-passe" : "Mostrar palavra-passe");
});

api.me()
    .then(() => window.location.replace("/"))
    .catch((error) => {
        if (!(error instanceof ApiError) || error.status !== 401) {
            errorBox.textContent = "Não foi possível contactar o servidor. Confirme se a aplicação está ligada.";
            errorBox.hidden = false;
        }
    });

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorBox.hidden = true;
    submitButton.disabled = true;
    submitButton.querySelector("span").textContent = "A validar acesso...";

    const formData = new FormData(form);
    try {
        await api.login(
            String(formData.get("username")).trim(),
            String(formData.get("password")),
        );
        window.location.replace("/");
    } catch (error) {
        errorBox.textContent = error.message || "Não foi possível iniciar sessão.";
        errorBox.hidden = false;
        submitButton.disabled = false;
        submitButton.querySelector("span").textContent = "Entrar no sistema";
        passwordInput.focus();
    }
});
