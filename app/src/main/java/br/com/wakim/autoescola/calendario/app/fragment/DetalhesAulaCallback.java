package br.com.wakim.autoescola.calendario.app.fragment;

import br.com.wakim.autoescola.calendario.app.model.Aula;

/**
 * Created by wakim on 13/09/14.
 */
public interface DetalhesAulaCallback {
	public void onAulaConcluidaToggle(Aula aula);
	public void onAulaDeleted(Aula aula);
}
