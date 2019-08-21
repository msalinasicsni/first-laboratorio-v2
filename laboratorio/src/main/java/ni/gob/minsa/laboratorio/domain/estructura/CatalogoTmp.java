// -----------------------------------------------
// Catalogo.java
// -----------------------------------------------

package ni.gob.minsa.laboratorio.domain.estructura;

import org.hibernate.annotations.DiscriminatorOptions;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name="catalogos", schema="general")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="DEPENDENCIA",discriminatorType = DiscriminatorType.STRING)
@DiscriminatorOptions(force = true)
public class CatalogoTmp extends BaseEntidadCatalogo implements Serializable {
	private static final long serialVersionUID = 1L;

    public CatalogoTmp() {

    }

    @Override
	public boolean equals(Object other) {
		
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof CatalogoTmp))
			return false;
		
		CatalogoTmp castOther = (CatalogoTmp) other;
		
		return (this.getCatalogoId() == castOther.getCatalogoId())
				&& (this.getCodigo().equals(castOther.getCodigo())) 
						&& (this.getValor().equals(castOther.getValor()));
	}
}