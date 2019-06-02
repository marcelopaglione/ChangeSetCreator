import changeset.ChangeSetCreator;
import domain.Liquidacao;

public class Run {
    public static void main(String[] args) {
        new ChangeSetCreator().create(Liquidacao.class, "marcelo.junior");
    }
}
