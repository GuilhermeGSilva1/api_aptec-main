// URL base da API Spring Boot
const API_URL = "http://localhost:8080";

// Função assíncrona responsável por processar o login do usuário
async function login(event) {
    event.preventDefault(); // Impede o comportamento padrão do formulário de recarregar a página ao submeter

    // Captura os elementos de entrada da tela de login
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");

    // Validação de preenchimento dos campos
    if (!usernameInput.value || !passwordInput.value) {
        alert("Por favor, preencha o usuário e a senha.");
        return;
    }

    try {
        // Realiza a requisição POST para o endpoint /login da API Spring Boot
        const response = await fetch(`${API_URL}/login`, {
            method: "POST", // Método HTTP de login
            headers: {
                "Content-Type": "application/json", // Informa que o corpo enviado é do tipo JSON
                "Accept": "application/json" // Informa que aceita respostas em JSON
            },
            // Converte o objeto JavaScript com as credenciais para uma String JSON
            body: JSON.stringify({
                username: usernameInput.value,
                password: passwordInput.value
            })
        });

        // Verifica se a requisição foi processada com sucesso (Status 200 OK)
        if (response.ok) {
            // Extrai o token JWT retornado no cabeçalho 'Authorization' da resposta
            const token = response.headers.get("Authorization");

            // Se o token foi retornado com sucesso
            if (token) {
                // Armazena a chave 'Authorization' e a String do token no localStorage do navegador
                localStorage.setItem("Authorization", token);

                alert("Login realizado com sucesso!"); // Notificação visual de sucesso

                // Redireciona o usuário para a página principal das tarefas
                window.location.href = "index.html";
            } else {
                alert("Erro: Token não retornado pelo servidor.");
            }
        } else {
            // Exibe mensagem caso as credenciais sejam inválidas (ex: erro 401 ou 403)
            alert("Usuário ou senha inválidos!");
        }
    } catch (error) {
        // Captura e exibe erros de conexão com o servidor
        console.error("Erro na requisição de login:", error);
        alert("Erro ao conectar com o servidor.");
    }
}