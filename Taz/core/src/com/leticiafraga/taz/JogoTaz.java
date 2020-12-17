package com.leticiafraga.taz;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.Random;

public class JogoTaz extends ApplicationAdapter {
	private final float VIRTUAL_HEIGHT = 1280;
	private final float VIRTUAL_WIDTH = 800;
	private float alturaDisp;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Texture canoBaixo;
	private Texture canoBaixo2;
	private Texture canoTopo;
	private Texture canoTopo2;
	private float espacoEntreCanos;
	private float espacoEntreCanos2;
	private int estadoJogo = 0;
	private Texture fundo;
	private Texture gameOver;
	private float gravidade = 0;
	private float larguraDisp;
	private boolean passouCano;
	private boolean passouCano2 = false;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private float posicaoCano2X = 0;
	private float posicaoCano2Y = 0;
	private float posicaoCanoX;
	private float posicaoCanoY;
	private float posicaoPassaroY = 0;
	private int posicaoPassou;
	Preferences preferences;
	private Random random;
	//shapeRenderer
	private Rectangle retanguloCanoBaixo;
	private Rectangle retanguloCanoBaixo2;
	private Rectangle retanguloCanoTopo;
	private Rectangle retanguloCanoTopo2;
	private Rectangle retanguloPassaro;
	private ShapeRenderer shapeRenderer;
	//sons
	Sound somColisao;
	Sound somPontuacao;
	Sound somVoando;
	private Texture[] taz;
	private Texture textoIniciar;
	BitmapFont textoMelhorPontuacao;
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	private float variacao = 0;
	private int velocidadeCanos;
	private Viewport viewport;

	public void create() {
		inicializarTexturas();
		inicializarObjetos();
	}

	public void render() {
		//Gdx.f0gl.glClear(16640);
		verificarEstadoJogo();
		validarPontos();
		desenharObjetos();
		detectarColisoes();

	}

	private void verificarEstadoJogo() {
		boolean toqueTela = Gdx.input.justTouched();
		if (estadoJogo == 0) {
			if (toqueTela) {
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		} else if (estadoJogo == 1) {
			if (toqueTela) {
				gravidade = -15;
				somVoando.play();
			}
			posicaoCanoX -= Gdx.graphics.getDeltaTime() * velocidadeCanos;
			posicaoCano2X -= Gdx.graphics.getDeltaTime() * velocidadeCanos;
			if (posicaoCanoX < - canoBaixo.getWidth()){
				posicaoCanoX = larguraDisp;
				posicaoCanoY = random.nextInt(800) - 400;
				espacoEntreCanos = random.nextInt(60) + 110;
				passouCano = false;
			}
			if (posicaoCano2X < - canoBaixo.getWidth()) {
				posicaoCano2X = larguraDisp;
				posicaoCano2Y = random.nextInt(800) - 400;
				espacoEntreCanos2 = random.nextInt(60) + 110;
				passouCano2 = false;
			}
			if (posicaoPassaroY > 0 || gravidade < 0) {

				if (posicaoPassaroY < gravidade) {
					posicaoPassaroY = 0;
				} else {
					posicaoPassaroY = posicaoPassaroY - gravidade;
				}
			}
			gravidade += 1;
		} else if (estadoJogo == 2) {
			variacao = 2;
			if (posicaoPassaroY > 0) {
				if (posicaoPassaroY < gravidade) {
					posicaoPassaroY = 0;
				} else {
					posicaoPassaroY = posicaoPassaroY - gravidade;
					gravidade += 1;
				}
			}
			if (pontos > pontuacaoMaxima) {
				pontuacaoMaxima = pontos;
				preferences.putInteger("pontuacaoMaxima", pontos);
				preferences.flush();
			}
			velocidadeCanos = 200;
			if (toqueTela) {
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoPassaroY = alturaDisp / 2;
				posicaoCanoX = larguraDisp;
				posicaoCano2X = (float) ((larguraDisp * 1.5) + canoBaixo.getWidth() / 2);
				posicaoCanoY = 0;
				posicaoCano2Y = 0;
				variacao = 0;
			}
		}
	}

	private void detectarColisoes() {
		retanguloPassaro.set( taz[0].getWidth() / 2 + 50, posicaoPassaroY + ( taz[0].getHeight() / 2),
				taz[0].getWidth() / 2,  taz[0].getHeight() / 2);
		retanguloCanoBaixo.set(posicaoCanoX + 10, (alturaDisp / 2 - canoBaixo.getHeight()) - espacoEntreCanos + posicaoCanoY,
				canoBaixo.getWidth() - 20, canoBaixo.getHeight());
		retanguloCanoTopo.set(posicaoCanoX + 10, (alturaDisp / 2) + espacoEntreCanos + posicaoCanoY,
				canoTopo.getWidth() - 20, canoTopo.getHeight());
		retanguloCanoBaixo2.set(posicaoCano2X + 10, (alturaDisp / 2 - canoBaixo.getHeight()) - espacoEntreCanos2 + posicaoCano2Y,
				canoBaixo.getWidth() - 20, canoBaixo.getHeight());
		retanguloCanoTopo2.set(posicaoCano2X + 10, alturaDisp / 2 + espacoEntreCanos2 + posicaoCano2Y,
				canoTopo.getWidth() - 20, canoTopo.getHeight());
		boolean colidiuCanoBaixo = Intersector.overlaps(retanguloPassaro, retanguloCanoBaixo);
		boolean colidiuCanoTopo = Intersector.overlaps(retanguloPassaro, retanguloCanoTopo);
		boolean colidiuCanoBaixo2 = Intersector.overlaps(retanguloPassaro, retanguloCanoBaixo2);
		boolean colidiuCanoTopo2 = Intersector.overlaps(retanguloPassaro, retanguloCanoTopo2);
		if ((colidiuCanoBaixo || colidiuCanoTopo || colidiuCanoBaixo2 || colidiuCanoTopo2 || posicaoPassaroY <= 0 || posicaoPassaroY > alturaDisp + 100) && estadoJogo == 1) {
			somColisao.play();
			estadoJogo = 2;
		}
	}

	private void desenharObjetos() {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo, 0, 0, larguraDisp, alturaDisp);
		batch.draw(canoBaixo, posicaoCanoX, alturaDisp/2 - canoBaixo.getHeight() - espacoEntreCanos + posicaoCanoY);
		batch.draw(canoTopo, posicaoCanoX, alturaDisp/2 + espacoEntreCanos + posicaoCanoY);
		batch.draw(canoBaixo, posicaoCano2X, alturaDisp/2 - canoBaixo.getHeight() - espacoEntreCanos2 + posicaoCano2Y);
		batch.draw(canoTopo, posicaoCano2X, (alturaDisp / 2) + espacoEntreCanos2 + posicaoCano2Y);
		batch.draw(taz[(int) variacao], 50, posicaoPassaroY);
		textoPontuacao.draw(batch, String.valueOf(pontos), (larguraDisp - 100) / 2, alturaDisp - 50);
		if (estadoJogo == 0) {

			batch.draw(textoIniciar, (larguraDisp -  textoIniciar.getWidth())/ 2, alturaDisp / 2);
		}
		if (estadoJogo == 2) {

			batch.draw(gameOver, (larguraDisp - (gameOver.getWidth())) / 2, (alturaDisp - (gameOver.getHeight())) / 2);
			textoReiniciar.draw(batch, "Toque na tela para reiniciar!", (larguraDisp / 2) - 180, (alturaDisp / 2) - gameOver.getHeight());
			textoMelhorPontuacao.draw(batch, "Melhor pontuação: " + pontuacaoMaxima + " pontos",
					larguraDisp/2 - 200, alturaDisp / 2 - (gameOver.getHeight() * 2));
		}
		batch.end();
	}

	private void inicializarTexturas() {
		taz = new Texture[3];
		taz[0] = new Texture("taz0.png");
		taz[1] = new Texture("taz1.png");
		taz[2] = new Texture("taz2.png");
		fundo = new Texture("fundo_petz.png");
		textoIniciar = new Texture("texto_iniciar.png");
		canoBaixo = new Texture("cano_macaco.png");
		canoTopo = new Texture("cano_macaco_topo.png");
		canoBaixo2 = new Texture("cano_macaco.png");
		canoTopo2 = new Texture("cano_macaco_topo.png");
		gameOver = new Texture("game_over.png");
	}

	private void inicializarObjetos() {
		batch = new SpriteBatch();
		random = new Random();
		larguraDisp = VIRTUAL_WIDTH;
		alturaDisp = VIRTUAL_HEIGHT;
		posicaoPassaroY = alturaDisp / 2;
		posicaoCanoX = larguraDisp;
		posicaoCano2X = (int) (larguraDisp * 1.5) + canoBaixo.getWidth()/2;
		espacoEntreCanos = 150;
		espacoEntreCanos2 = 150;
		posicaoCanoY = 0;
		posicaoCano2Y = 0;
		posicaoPassou = 50 - taz[0].getWidth();
		velocidadeCanos = 200;

		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(8);
		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);
		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		retanguloPassaro = new Rectangle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoTopo = new Rectangle();
		retanguloCanoBaixo2 = new Rectangle();
		retanguloCanoTopo2 = new Rectangle();

		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferences = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferences.getInteger("pontuacaoMaxima");
		camera = new OrthographicCamera();
		camera.position.set(larguraDisp/2, alturaDisp/2, 0);
		viewport = new StretchViewport(larguraDisp, alturaDisp, camera);
	}

	public void validarPontos() {
		if (posicaoCanoX < posicaoPassou && !passouCano) {
			pontos ++;
			passouCano = true;
			velocidadeCanos += 20;
			somPontuacao.play();
		}
		if (posicaoCano2X < posicaoPassou && !passouCano2) {
			pontos ++;
			passouCano2 = true;
			velocidadeCanos += 10;
			somPontuacao.play();
		}
		if (estadoJogo != 2) {
			variacao = variacao + (Gdx.graphics.getDeltaTime() * 10.0f);
			if (variacao > 2) {
				variacao = 0;
			}
		}
	}

	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	public void dispose() {
	}
}
