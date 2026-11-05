 // Define o endereço base da API
 const API_URL = "http://localhost:8080";

 // Executa a busca das tarefas quando a página termina de carregar
 document.addEventListener("DOMContentLoaded", () => {
     getTasks();
 });
 
 // Recupera o token de autenticação salvo no navegador
 function getAuthToken() {
     const token = localStorage.getItem("Authorization");
 
     // Verifica se o usuário possui um token de autenticação
     if (!token) {
         alert("Acesso não autorizado! Faça login primeiro.");
         window.location.href = "login.html";
         return null;
     }
 
     return token;
 }
 
 // Busca as tarefas cadastradas pelo usuário na API
 async function getTasks() {
     const token = getAuthToken();
 
     // Interrompe a função caso não exista um token
     if (!token) return;
 
     const loading = document.getElementById("loading");
 
     // Exibe o indicador de carregamento
     if (loading) {
         loading.style.display = "block";
     }
 
     try {
         // Envia uma requisição GET para buscar as tarefas do usuário
         const response = await fetch(`${API_URL}/task/user`, {
             method: "GET",
             headers: {
                 "Authorization": token,
                 "Accept": "application/json"
             }
         });
 
         // Verifica se a requisição foi realizada corretamente
         if (response.ok) {
             const tasks = await response.json();
             renderTasks(tasks);
         } else if (response.status === 401 || response.status === 403) {
             alert("Sessão expirada. Faça login novamente.");
             logout();
         } else {
             alert("Erro ao buscar tarefas.");
             console.error("Erro na busca:", response.status);
         }
 
     } catch (error) {
         // Exibe erros relacionados à conexão com a API
         console.error("Erro na busca de tarefas:", error);
         alert("Erro de conexão com o servidor.");
 
     } finally {
         // Oculta o indicador de carregamento após a requisição
         if (loading) {
             loading.style.display = "none";
         }
     }
 }
 
 // Exibe na página as tarefas recebidas da API
 function renderTasks(tasks) {
     const tasksList = document.getElementById("tasks-list");
 
     // Verifica se a lista existe no HTML
     if (!tasksList) return;
 
     // Limpa a lista antes de adicionar as tarefas atualizadas
     tasksList.innerHTML = "";
 
     // Verifica se existem tarefas cadastradas
     if (!Array.isArray(tasks) || tasks.length === 0) {
         tasksList.innerHTML = `
             <li class="list-group-item text-center text-muted">
                 Nenhuma tarefa encontrada.
             </li>
         `;
         return;
     }
 
     // Percorre todas as tarefas recebidas da API
     tasks.forEach(task => {
         // Cria o elemento que representa uma tarefa
         const li = document.createElement("li");
         li.className = "list-group-item d-flex justify-content-between align-items-center py-3";
 
         // Cria o elemento responsável por exibir a descrição
         const descriptionSpan = document.createElement("span");
         descriptionSpan.className = "me-3 flex-grow-1 text-break";
         descriptionSpan.textContent = task.description;
 
         // Cria um container para organizar os botões
         const btnGroup = document.createElement("div");
         btnGroup.className = "btn-group btn-group-sm flex-shrink-0";
 
         // Cria o botão de edição da tarefa
         const editBtn = document.createElement("button");
         editBtn.type = "button";
         editBtn.className = "btn btn-outline-primary";
         editBtn.title = "Editar tarefa";
         editBtn.innerHTML = `<i class="bi bi-pencil-fill"></i>`;
         editBtn.onclick = () => openEditModal(task);
 
         // Cria o botão de exclusão da tarefa
         const deleteBtn = document.createElement("button");
         deleteBtn.type = "button";
         deleteBtn.className = "btn btn-outline-danger";
         deleteBtn.title = "Excluir tarefa";
         deleteBtn.innerHTML = `<i class="bi bi-trash-fill"></i>`;
         deleteBtn.onclick = () => deleteTask(task.id);
 
         // Adiciona os botões ao grupo
         btnGroup.appendChild(editBtn);
         btnGroup.appendChild(deleteBtn);
 
         // Adiciona a descrição e os botões ao elemento da tarefa
         li.appendChild(descriptionSpan);
         li.appendChild(btnGroup);
 
         // Adiciona a tarefa completa à lista do HTML
         tasksList.appendChild(li);
     });
 }
 
 // Cadastra uma nova tarefa na API
 async function createTask(event) {
     // Impede que a página seja recarregada ao enviar o formulário
     event.preventDefault();
 
     const token = getAuthToken();
 
     // Interrompe a função caso não exista um token
     if (!token) return;
 
     // Recupera o campo de descrição existente no HTML
     const inputDescription = document.getElementById("taskDescription");
 
     // Verifica se o campo foi encontrado
     if (!inputDescription) {
         console.error("Campo taskDescription não encontrado no HTML.");
         return;
     }
 
     // Recupera a descrição digitada e remove espaços desnecessários
     const description = inputDescription.value.trim();
 
     // Impede o cadastro de tarefas sem descrição
     if (!description) {
         alert("Digite uma descrição para a tarefa.");
         inputDescription.focus();
         return;
     }
 
     try {
         // Envia uma requisição POST para cadastrar a nova tarefa
         const response = await fetch(`${API_URL}/task`, {
             method: "POST",
             headers: {
                 "Authorization": token,
                 "Content-Type": "application/json",
                 "Accept": "application/json"
             },
 
             // Converte a descrição para o formato JSON
             body: JSON.stringify({
                 description: description
             })
         });
 
         // Verifica se a tarefa foi cadastrada corretamente
         if (response.ok) {
             inputDescription.value = "";
             await getTasks();
 
         } else if (response.status === 401 || response.status === 403) {
             alert("Sessão expirada ou sem permissão. Faça login novamente.");
             logout();
 
         } else {
             // Exibe detalhes do erro retornado pela API
             const errorMessage = await response.text();
             console.error("Erro ao cadastrar tarefa:", response.status, errorMessage);
             alert("Não foi possível cadastrar a tarefa. Verifique os dados e tente novamente.");
         }
 
     } catch (error) {
         // Trata possíveis erros de conexão com o servidor
         console.error("Erro ao criar tarefa:", error);
         alert("Erro de conexão com o servidor.");
     }
 }
 
 // Abre a janela modal e preenche os campos com os dados da tarefa selecionada
 function openEditModal(task) {
     // Preenche o campo oculto com o ID da tarefa
     document.getElementById("edit-task-id").value = task.id;
 
     // Preenche o campo com a descrição atual da tarefa
     document.getElementById("edit-task-description").value = task.description;
 
     // Recupera a janela modal do Bootstrap e a exibe na tela
     const modalElement = document.getElementById("editTaskModel");
     const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
 
     modal.show();
 }
 
 // Atualiza uma tarefa existente na API
 async function update(event) {
     // Impede o recarregamento da página ao enviar o formulário
     event.preventDefault();
 
     const token = getAuthToken();
 
     // Interrompe a função caso não exista um token
     if (!token) return;
 
     // Recupera o ID da tarefa selecionada
     const taskId = document.getElementById("edit-task-id").value;
 
     // Recupera a nova descrição digitada pelo usuário
     const newDescription = document.getElementById("edit-task-description").value.trim();
 
     // Verifica se a nova descrição foi preenchida
     if (!newDescription) {
         alert("Digite uma descrição para a tarefa.");
         return;
     }
 
     try {
         // Envia uma requisição PUT para atualizar a tarefa selecionada
         const response = await fetch(`${API_URL}/task/${taskId}`, {
             method: "PUT",
             headers: {
                 "Authorization": token,
                 "Content-Type": "application/json",
                 "Accept": "application/json"
             },
 
             // Envia a nova descrição no formato JSON
             body: JSON.stringify({
                 description: newDescription
             })
         });
 
         // Verifica se a atualização foi realizada corretamente
         if (response.ok) {
             const modalElement = document.getElementById("editTaskModel");
 
             // Fecha a janela de edição após salvar as alterações
             bootstrap.Modal.getOrCreateInstance(modalElement).hide();
 
             // Atualiza a lista para exibir a descrição modificada
             await getTasks();
 
         } else if (response.status === 401 || response.status === 403) {
             alert("Sessão expirada ou sem permissão. Faça login novamente.");
             logout();
 
         } else {
             console.error("Erro ao atualizar tarefa:", response.status);
             alert("Não foi possível atualizar a tarefa.");
         }
 
     } catch (error) {
         // Trata possíveis erros de conexão durante a atualização
         console.error("Erro ao atualizar tarefa:", error);
         alert("Erro de conexão com o servidor.");
     }
 }
 
 // Exclui uma tarefa cadastrada na API
 async function deleteTask(id) {
     // Solicita confirmação antes de excluir a tarefa
     const confirmDelete = confirm("Deseja realmente excluir esta tarefa?");
 
     // Cancela a exclusão caso o usuário não confirme
     if (!confirmDelete) return;
 
     const token = getAuthToken();
 
     // Interrompe a função caso não exista um token
     if (!token) return;
 
     try {
         // Envia uma requisição DELETE utilizando o ID da tarefa
         const response = await fetch(`${API_URL}/task/${id}`, {
             method: "DELETE",
             headers: {
                 "Authorization": token,
                 "Accept": "application/json"
             }
         });
 
         // Verifica se a exclusão foi realizada corretamente
         if (response.ok) {
             await getTasks();
 
         } else if (response.status === 401 || response.status === 403) {
             alert("Sessão expirada ou sem permissão. Faça login novamente.");
             logout();
 
         } else {
             console.error("Erro ao excluir tarefa:", response.status);
             alert("Não foi possível excluir a tarefa.");
         }
 
     } catch (error) {
         // Trata possíveis erros de conexão durante a exclusão
         console.error("Erro ao excluir tarefa:", error);
         alert("Erro de conexão com o servidor.");
     }
 }
 
 // Encerra a sessão do usuário
 function logout() {
     // Remove o token de autenticação salvo no navegador
     localStorage.removeItem("Authorization");
 
     // Redireciona o usuário para a página de login
     window.location.href = "login.html";
 }
 