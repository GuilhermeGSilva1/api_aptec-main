// URL base da API
const API_URL = "http://localhost:8080";

// Evento executado automaticamente assim que todo a estrutura do documento HTML for carregada
document.addEventListener("DOMContentLoaded", () => {
    getTasks(); // Chama a função para buscar e listar as tarefas do usuário logado
});

// Função assíncrona responsável por buscar e exibir as tarefas do usuário autenticado
async function getTasks() {
    // Recupera a String do token JWT salva no localStorage durante o login
    const token = localStorage.getItem("Authorization");

    // Se o token não existir (usuário tentando acessar diretamente a index sem estar logado)
    if (!token) {
        alert("Acesso não autorizado! Faça login primeiro.");
        window.location.href = "login.html"; // Redireciona imediatamente para o login
        return; // Interrompe a execução
    }

    // Exibe o spinner de carregamento na tela
    document.getElementById("loading").style.display = "block";

    try {
        // Envia requisição GET para o endpoint /task/user passando o token no cabeçalho Authorization
        const response = await fetch(`${API_URL}/task/user`, {
            method: "GET", // Método HTTP de busca
            headers: {
                "Authorization": token, // Envia o token 'Bearer <token>' gravado no localStorage
                "Content-Type": "application/json" // Tipo da requisição
            }
        });

        // Se o token for aceito pela API (Status 200 OK)
        if (response.ok) {
            // Converte a resposta recebida (TaskProjection) em um array JavaScript
            const tasks = await response.json();

            // Renderiza as tarefas na página
            renderTasks(tasks);
        } else if (response.status === 403 || response.status === 401) {
            // Se o token tiver expirado ou for inválido
            alert("Sessão expirada. Por favor, faça login novamente.");
            logout(); // Executa o encerramento da sessão
        } else {
            alert("Erro ao buscar tarefas.");
        }
    } catch (error) {
        console.error("Erro na busca de tarefas:", error);
        alert("Erro de conexão com o servidor.");
    } finally {
        // Oculta o spinner de carregamento após concluir a requisição
        document.getElementById("loading").style.display = "none";
    }
}

// Função responsável por montar o HTML da lista de tarefas recebida da API
function renderTasks(tasks) {
    const tasksList = document.getElementById("tasks-list"); // Seleciona o elemento <ul> no DOM
    tasksList.innerHTML = ""; // Limpa a lista antes de inserir novos elementos

    // Se a lista retornada estiver vazia
    if (tasks.length === 0) {
        tasksList.innerHTML = `<li class="list-group-item text-center text-muted">Nenhuma tarefa encontrada.</li>`;
        return;
    }

    // Percorre cada objeto do tipo TaskProjection (possuindo apenas id e description)
    tasks.forEach(task => {
        // Cria um elemento de lista <li> do Bootstrap
        const li = document.createElement("li");
        li.className = "list-group-item d-flex justify-content-between align-items-center";
        
        // Define o texto da descrição da tarefa na lista
        li.innerHTML = `<span>${task.description}</span>`;

        // Adiciona o item à lista principal no HTML
        tasksList.appendChild(li);
    });
}

// Função responsável por encerrar a sessão do usuário no navegador
function logout() {
    // Remove a chave 'Authorization' contendo o token do localStorage
    localStorage.removeItem("Authorization");

    // Redireciona o usuário para a página de login
    window.location.href = "login.html";
}