import changeset.ChangeSetCreator;
import domain.Liquidacao;

public class Run {
    public static void main(String[] args) throws Exception{
        new ChangeSetCreator().create(Liquidacao.class, "marcelo.junior");
    }
}
