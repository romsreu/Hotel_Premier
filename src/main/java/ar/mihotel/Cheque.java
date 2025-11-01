package ar.mihotel;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor



public class Cheque {
    private int id_medio_pago;
    private int nro_cheque;
    private String banco;
    private String plaza;
    private Date fecha_cobro;
    private Boolean es_propio;
}
