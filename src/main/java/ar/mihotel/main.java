package ar.mihotel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class main {
    public static void main(String[] args) {
        Persona p1 = new Persona(1, "Agustin", "Scotta", 123456789);
        System.out.println(p1);

        Persona p2 = new Persona();
        p2.setNombre("Juan");
        p2.setApellido("Perez");
        System.out.println(p2.getNombre() + " " + p2.getApellido());
    }
}
