package br.com.wakim.autoescola.calendario.app.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by wakim on 10/08/14.
 */
@Table(name = "disciplina", id = BaseColumns._ID)
public class Disciplina extends Model implements Parcelable {

	public static final String TOTAL_AULAS_CONCLUIDAS = "total_aulas_concluidas",
							   TOTAL_AULAS_RESTANTES = "total_aulas_restantes",
							   NOME = "nome",
							   LIMITE = "limite",
							   SIMBOLO = "simbolo",
							   COR = "cor";

	@Column(name = TOTAL_AULAS_CONCLUIDAS)
	Integer totalAulasConcluidas;

	@Column(name = TOTAL_AULAS_RESTANTES)
	Integer totalAulasRestantes;

	@Column(name = NOME, notNull = true)
	String nome;

	@Column(name = LIMITE, notNull = true)
	Integer limite;

	@Column(name = SIMBOLO)
	String simbolo;

	@Column(name = COR)
	Integer cor;

	public Disciplina() {}

	public Disciplina(Parcel in) {
		setId(in.readLong());
		setTotalAulasConcluidas(in.readInt());
		setTotalAulasRestantes(in.readInt());
		setNome(in.readString());
		setLimite(in.readInt());
		setSimbolo(in.readString());
		setCor(in.readInt());
	}

	public void setId(Long id) {
		try {

			if(id == -123l) {
				id = null;
			}

			Field mId = Model.class.getDeclaredField("mId");

			mId.setAccessible(true);
			mId.set(this, id);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Integer getLimite() {
		return limite;
	}

	public void setLimite(Integer limite) {
		this.limite = limite;
	}

	public Integer getTotalAulasConcluidas() {
		return totalAulasConcluidas;
	}

	public void setTotalAulasConcluidas(Integer totalAulasConcluidas) {
		this.totalAulasConcluidas = totalAulasConcluidas;
	}

	public Integer getTotalAulasRestantes() {
		return totalAulasRestantes;
	}

	public void setTotalAulasRestantes(Integer totalAulasRestantes) {
		this.totalAulasRestantes = totalAulasRestantes;
	}

	public String getSimbolo() {
		return simbolo;
	}

	public void setSimbolo(String simbolo) {
		this.simbolo = simbolo;
	}

	public Integer getCor() {
		return cor;
	}

	public void setCor(Integer cor) {
		this.cor = cor;
	}

	public Integer calcularTotalAulasConcluidas() {

		if(getId() == null) {
			return 0;
		}

		return totalAulasConcluidas = contarAulas(true);
	}

	public void calcularTotalAulasRestantes() {
		if(limite != null) {

			Integer concluidas = getTotalAulasConcluidas();

			if(concluidas == null) {
				concluidas = calcularTotalAulasConcluidas();
			}

			totalAulasRestantes = limite - concluidas;
		}
	}

	synchronized int contarAulas(boolean concluida) {
		return new Select().from(Aula.class).where(Aula.CONCLUIDA + " = ?", concluida).and(Aula.DISCIPLINA + " = ?", getId()).count();
	}

	public List<Aula> getAulas() {
		return new Select().from(Aula.class).where(Cache.getTableName(Aula.class) + "." + Aula.DISCIPLINA + "=?", getId()).orderBy(Aula.DATA + " ASC").execute();
	}

	public static int total() {
		return new Select().from(Disciplina.class).count();
	}

	public Long saveAndCalculate() {

		calcularTotalAulasConcluidas();
		calcularTotalAulasRestantes();

		return super.save();
	}

	// Metodos da interface/protocolo Parcelable

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Long id = getId();

		if(id == null) {
			id = -123l;
		}

		dest.writeLong(id);
		dest.writeInt(getTotalAulasConcluidas());
		dest.writeInt(getTotalAulasRestantes());
		dest.writeString(getNome());
		dest.writeInt(getLimite());
		dest.writeString(getSimbolo());
		dest.writeInt(getCor());
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Disciplina createFromParcel(Parcel in) {
			if(! Cache.isInitialized()) {
				return null;
			}

			return new Disciplina(in);
		}

		public Disciplina[] newArray(int size) {
			return new Disciplina[size];
		}
	};
}
