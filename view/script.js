const url = "http://localhost:8080/task/user/1"; 

function hideLoader() {
    const loadingElement = document.getElementById("loading");
    if (loadingElement) {
        loadingElement.style.display = "none"; 
    }
}

function show(tasks) {
    let tab = `
        <thead>
            <tr>
                <th scope="col">#</th>
                <th scope="col">Descrição</th>
                <th scope="col">Usuário</th>
                <th scope="col">User ID</th>
            </tr>
        </thead>
        <tbody>
    `;

    if (!Array.isArray(tasks) || tasks.length === 0) {
        tab += `
            <tr>
                <td colspan="4" class="text-center">Nenhuma tarefa encontrada.</td>
            </tr>
        `;
    } else {
        for (let task of tasks) {
            const username = task.user ? task.user.username : "N/A";
            const userId = task.user ? task.user.id : "N/A";

            tab += `
                <tr>
                    <td scope="row">${task.id}</td>
                    <td>${task.description}</td>
                    <td>${username}</td>
                    <td>${userId}</td>
                </tr>
            `;
        }
    }

    tab += `</tbody>`;
    document.getElementById("tasks").innerHTML = tab;
}

// Declaração explícita da função assíncrona getAPI
async function getAPI(apiUrl) {
    try {
        const response = await fetch(apiUrl, { method: "GET" }); 

        if (!response.ok) {
            throw new Error(`Erro de rede: ${response.status}`);
        }

        const data = await response.json(); 
        hideLoader();
        show(data); 
    } catch (error) {
        console.error("Erro na requisição:", error);
        hideLoader();
        document.getElementById("tasks").innerHTML = `
            <tbody>
                <tr>
                    <td colspan="4" class="text-center text-danger">
                        Erro ao carregar as tarefas. Verifique se a API está rodando e com o CORS ativo.
                    </td>
                </tr>
            </tbody>
        `;
    }
}

// Chamada da função garantindo que ela existe
getAPI(url);