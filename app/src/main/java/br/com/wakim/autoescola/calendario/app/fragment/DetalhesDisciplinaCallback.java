package br.com.wakim.autoescola.calendario.app.fragment;

import br.com.wakim.autoescola.calendario.app.model.Aula;

/**
 * Created by wakim on 25/08/14.
 */
public interface DetalhesDisciplinaCallback extends DetalhesAulaCallback {
	public void onNovaAula();
	public void onAulaClicked(Aula aula);
}
