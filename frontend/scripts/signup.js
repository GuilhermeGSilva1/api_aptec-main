// URL base da API Spring Boot
const API_URL = "http://localhost:8080";

// Função assíncrona responsável por cadastrar um novo usuário na API
async function signup(event) {
    event.preventDefault(); // Evita a atualização padrão do formulário HTML

    // Captura os valores informados nos campos de cadastro
    const usernameInput = document.getElementById("username").value;
    const passwordInput = document.getElementById("password").value;

    try {
        // Envia requisição POST para o endpoint /user para cadastrar o usuário via UserCreateDTO
        const response = await fetch(`${API_URL}/user`, {
            method: "POST", // Método de criação
            headers: {
                "Content-Type": "application/json", // Define o envio de corpo JSON
                "Accept": "application/json" // Aceita respostas JSON
            },
            // Converte o username e password informados em uma String JSON
            body: JSON.stringify({
                username: usernameInput,
                password: passwordInput
            })
        });

        // Se a resposta retornar Status 201 Created
        if (response.ok) {
            alert("Usuário cadastrado com sucesso! Faça login para continuar.");

            // Redireciona o novo usuário para a página de login
            window.location.href = "login.html";
        } else {
            // Exibe mensagem caso o cadastro falhe (ex: usuário já existente ou dados inválidos)
            alert("Erro ao realizar cadastro. Verifique os dados fornecidos.");
        }
    } catch (error) {
        // Captura e exibe falhas de conexão no console
        console.error("Erro na requisição de cadastro:", error);
        alert("Erro ao conectar com o servidor.");
    }
}