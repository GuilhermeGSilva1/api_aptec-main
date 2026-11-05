
 // URL base da API
 const API_URL = "http://localhost:8080";

 // Executa a busca das tarefas assim que a página terminar de carregar
 document.addEventListener("DOMContentLoaded", () => {
     getTasks();
 });
 
 // Função responsável por verificar e recuperar o token JWT do usuário
 function getAuthToken() {
     const token = localStorage.getItem("Authorization"); // Recupera o token salvo no navegador
 
     // Verifica se o usuário está autenticado
     if (!token) {
         alert("Acesso não autorizado! Faça login primeiro.");
         window.location.href = "login.html"; // Redireciona para a página de login
         return null;
     }
 
     return token; // Retorna o token para autenticar as requisições
 }
 
 // Função responsável por buscar as tarefas do usuário na API
 async function getTasks() {
     const token = getAuthToken(); // Recupera o token de autenticação
 
     if (!token) return; // Interrompe a execução caso não exista token
 
     document.getElementById("loading").style.display = "block"; // Exibe o indicador de carregamento
 
     try {
         // Envia uma requisição GET para buscar as tarefas do usuário autenticado
         const response = await fetch(`${API_URL}/task/user`, {
             method: "GET",
             headers: {
                 "Authorization": token, // Envia o token JWT para autenticação
                 "Content-Type": "application/json", // Define o tipo de conteúdo da requisição
                 "Accept": "application/json" // Informa que a resposta esperada é em JSON
             }
         });
 
         // Verifica se a API retornou as tarefas corretamente
         if (response.ok) {
             const tasks = await response.json(); // Converte a resposta da API para JSON
             renderTasks(tasks); // Exibe as tarefas na página
         } else if (response.status === 403 || response.status === 401) {
             alert("Sessão expirada. Por favor, faça login novamente.");
             logout(); // Encerra a sessão do usuário
         } else {
             alert("Erro ao buscar tarefas.");
         }
     } catch (error) {
         console.error("Erro na busca de tarefas:", error);
         alert("Erro de conexão com o servidor.");
     } finally {
         document.getElementById("loading").style.display = "none"; // Oculta o indicador de carregamento
     }
 }
 
 // Função responsável por exibir as tarefas recebidas da API
 function renderTasks(tasks) {
     const tasksList = document.getElementById("tasks-list"); // Seleciona a lista de tarefas no HTML
     tasksList.innerHTML = ""; // Limpa a lista antes de adicionar as tarefas
 
     // Verifica se existem tarefas para exibir
     if (!Array.isArray(tasks) || tasks.length === 0) {
         tasksList.innerHTML = `<li class="list-group-item text-center text-muted">Nenhuma tarefa encontrada.</li>`;
         return;
     }
 
     // Percorre todas as tarefas recebidas da API
     tasks.forEach(task => {
         const li = document.createElement("li"); // Cria um elemento para representar a tarefa
         li.className = "list-group-item d-flex justify-content-between align-items-center py-3"; // Define a aparência do elemento
 
         const descriptionSpan = document.createElement("span"); // Cria o elemento que exibirá a descrição
         descriptionSpan.className = "me-3 flex-grow-1 text-break"; // Define a organização e a aparência do texto
         descriptionSpan.textContent = task.description; // Adiciona a descrição da tarefa
 
         const btnGroup = document.createElement("div"); // Cria um container para organizar os botões
         btnGroup.className = "btn-group btn-group-sm flex-shrink-0"; // Define a aparência do grupo de botões
 
         // Cria o botão responsável por editar a tarefa
         const editBtn = document.createElement("button");
         editBtn.className = "btn btn-outline-primary"; // Define a aparência do botão
         editBtn.title = "Editar tarefa"; // Define a descrição exibida ao passar o mouse
         editBtn.innerHTML = `<i class="bi bi-pencil-fill"></i>`; // Adiciona o ícone de edição
         editBtn.onclick = () => openEditModal(task); // Abre a janela de edição com os dados da tarefa
 
         // Cria o botão responsável por excluir a tarefa
         const deleteBtn = document.createElement("button");
         deleteBtn.className = "btn btn-outline-danger"; // Define a aparência do botão
         deleteBtn.title = "Excluir tarefa"; // Define a descrição exibida ao passar o mouse
         deleteBtn.innerHTML = `<i class="bi bi-trash-fill"></i>`; // Adiciona o ícone de exclusão
         deleteBtn.onclick = () => deleteTask(task.id); // Executa a exclusão utilizando o ID da tarefa
 
         btnGroup.appendChild(editBtn); // Adiciona o botão de edição ao grupo
         btnGroup.appendChild(deleteBtn); // Adiciona o botão de exclusão ao grupo
 
         li.appendChild(descriptionSpan); // Adiciona a descrição ao elemento da tarefa
         li.appendChild(btnGroup); // Adiciona os botões ao elemento da tarefa
 
         tasksList.appendChild(li); // Adiciona a tarefa completa à lista do HTML
     });
 }
 
 // Função responsável por criar uma nova tarefa na API
 async function createTask(event) {
     event.preventDefault(); // Impede o recarregamento da página ao enviar o formulário
 
     const token = getAuthToken(); // Recupera o token de autenticação
     if (!token) return; // Interrompe a execução caso não exista token
 
     const inputDescription = document.getElementById("taskDescription"); // Seleciona o campo de descrição
     const description = inputDescription.value.trim(); // Recupera a descrição e remove espaços desnecessários
 
     // Verifica se o campo de descrição foi preenchido
     if (!description) {
         alert("A descrição da tarefa não pode estar vazia.");
         return;
     }
 
     try {
         // Envia uma requisição POST para cadastrar uma nova tarefa
         const response = await fetch(`${API_URL}/task`, {
             method: "POST",
             headers: {
                 "Authorization": token, // Envia o token JWT para autenticação
                 "Content-Type": "application/json", // Define o tipo de conteúdo enviado
                 "Accept": "application/json" // Informa que a resposta esperada é em JSON
             },
             body: JSON.stringify({ description }) // Converte a descrição da tarefa para JSON
         });
 
         // Verifica se a tarefa foi criada corretamente
         if (response.ok) {
             inputDescription.value = ""; // Limpa o campo após o cadastro
             getTasks(); // Atualiza a lista de tarefas
         } else if (response.status === 401 || response.status === 403) {
             alert("Sessão expirada. Por favor, faça login novamente.");
             logout(); // Encerra a sessão do usuário
         } else {
             alert("Não foi possível criar a tarefa. Verifique os dados inseridos.");
         }
     } catch (error) {
         console.error("Erro ao criar tarefa:", error);
         alert("Erro de conexão com o servidor.");
     }
 }
 
 // Função responsável por abrir a janela de edição e preencher os campos com os dados da tarefa
 function openEditModal(task) {
     document.getElementById("edit-task-id").value = task.id; // Preenche o campo oculto com o ID da tarefa
     document.getElementById("edit-task-description").value = task.description; // Preenche o campo com a descrição atual
 
     // Cria e exibe a janela modal do Bootstrap
     const modalElement = document.getElementById("editTaskModel");
     const modal = new bootstrap.Modal(modalElement);
     modal.show();
 }
 
 // Função responsável por atualizar uma tarefa existente na API
 async function update(event) {
     event.preventDefault(); // Impede o recarregamento da página ao enviar o formulário
 
     const token = getAuthToken(); // Recupera o token de autenticação
     if (!token) return; // Interrompe a execução caso não exista token
 
     const taskId = document.getElementById("edit-task-id").value; // Recupera o ID da tarefa selecionada
     const newDescription = document.getElementById("edit-task-description").value.trim(); // Recupera a nova descrição
 
     // Verifica se a nova descrição foi preenchida
     if (!newDescription) {
         alert("A descrição da tarefa não pode estar vazia.");
         return;
     }
 
     try {
         // Envia uma requisição PUT para atualizar a tarefa selecionada
         const response = await fetch(`${API_URL}/task/${taskId}`, {
             method: "PUT",
             headers: {
                 "Authorization": token, // Envia o token JWT para autenticação
                 "Content-Type": "application/json", // Define o tipo de conteúdo enviado
                 "Accept": "application/json" // Informa que a resposta esperada é em JSON
             },
             body: JSON.stringify({ description: newDescription }) // Envia a nova descrição em JSON
         });
 
         // Verifica se a atualização foi realizada corretamente
         if (response.ok) {
             bootstrap.Modal.getInstance(document.getElementById("editTaskModel")).hide(); // Fecha a janela de edição
             getTasks(); // Atualiza a lista de tarefas
         } else if (response.status === 403 || response.status === 401) {
             alert("Sessão expirada. Por favor, faça login novamente.");
             logout(); // Encerra a sessão do usuário
         } else {
             alert("Erro ao atualizar tarefa.");
         }
     } catch (error) {
         console.error("Erro ao atualizar tarefa:", error);
         alert("Erro de conexão com o servidor.");
     }
 }
 
 // Função responsável por excluir uma tarefa da API
 async function deleteTask(id) {
     const confirmDelete = confirm("Deseja realmente excluir esta tarefa?"); // Solicita confirmação antes da exclusão
     if (!confirmDelete) return; // Interrompe a execução caso o usuário cancele
 
     const token = getAuthToken(); // Recupera o token de autenticação
     if (!token) return; // Interrompe a execução caso não exista token
 
     try {
         // Envia uma requisição DELETE para excluir a tarefa pelo ID
         const response = await fetch(`${API_URL}/task/${id}`, {
             method: "DELETE",
             headers: {
                 "Authorization": token, // Envia o token JWT para autenticação
                 "Accept": "application/json" // Informa que a resposta esperada é em JSON
             }
         });
 
         // Verifica se a exclusão foi realizada corretamente
         if (response.ok) {
             getTasks(); // Atualiza a lista após a exclusão
         } else if (response.status === 401 || response.status === 403) {
             alert("Sessão expirada ou sem permissão para excluir esta tarefa.");
             logout(); // Encerra a sessão do usuário
         } else {
             alert("Erro ao excluir tarefa.");
         }
     } catch (error) {
         console.error("Erro ao excluir tarefa:", error);
         alert("Erro de conexão com o servidor.");
     }
 }
 
 // Função responsável por encerrar a sessão do usuário
 function logout() {
     localStorage.removeItem("Authorization"); // Remove o token JWT salvo no navegador
     window.location.href = "login.html"; // Redireciona o usuário para a página de login
 }
 