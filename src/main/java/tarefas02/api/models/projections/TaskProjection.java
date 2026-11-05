package tarefas02.api.models.projections;

public interface TaskProjection {

    //Método getter para mapear e extrair apenas o Id da tarefa
    Long getId();

    //Método getter para mapear e extrair a descrição da tarefa. 
    String getDescription();
}

