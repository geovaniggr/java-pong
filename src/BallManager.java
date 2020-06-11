import java.awt.Color;
import java.lang.reflect.*;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Classe que gerencia uma ou mais bolas presentes em uma partida. Esta classe é
 * a responsável por instanciar e gerenciar a bola principal do jogo (aquela que
 * existe desde o ínicio de uma partida), assim como eventuais bolas extras que
 * apareçam no decorrer da partida. Esta classe também deve gerenciar a
 * interação da(s) bola(s) com os alvos, bem como a aplicação dos efeitos
 * produzidos para cada tipo de alvo atingido.
 */

public class BallManager {

	private IBall theBall = null;

	private Class<?> ballClass = null;

	private ConcurrentLinkedQueue<IBall> balls = new ConcurrentLinkedQueue<>();

	public BallManager(String className) {
		try {
			ballClass = Class.forName(className);
		} catch (Exception e) {

			System.out.println("Classe '" + className + "' não reconhecida... Usando 'Ball' como classe padrão.");
			ballClass = Ball.class;
		}
	}

	/**
	 * Recebe as componetes x e y de um vetor, e devolve as componentes x e y do
	 * vetor normalizado (isto é, com comprimento igual a 1.0).
	 * 
	 * @param x componente x de um vetor que representa uma direção.
	 * @param y componente y de um vetor que represetna uma direção.
	 * @return array contendo dois valores double que representam as componentes x
	 *         (índice 0) e y (índice 1) do vetor normalizado (unitário).
	 */
	private double[] normalize(double x, double y) {

		double length = Math.sqrt(x * x + y * y);

		return new double[] { x / length, y / length };
	}

	/**
	 * Cria uma instancia de bola, a partir do tipo (classe) cujo nome foi passado
	 * ao construtor desta classe. O vetor direção definido por (vx, vy) não precisa
	 * estar normalizado. A implemntação do método se encarrega de fazer a
	 * normalização.
	 * 
	 * @param cx     coordenada x da posição inicial da bola (centro do retangulo
	 *               que a representa).
	 * @param cy     coordenada y da posição inicial da bola (centro do retangulo
	 *               que a representa).
	 * @param width  largura do retangulo que representa a bola.
	 * @param height altura do retangulo que representa a bola.
	 * @param color  cor da bola.
	 * @param speed  velocidade da bola (em pixels por millisegundo).
	 * @param vx     componente x do vetor (não precisa ser unitário) que representa
	 *               a direção da bola.
	 * @param vy     componente y do vetor (não precisa ser unitário) que representa
	 *               a direção da bola.
	 */

	private IBall createBallInstance(double cx, double cy, double width, double height, Color color, double speed,
			double vx, double vy) {

		IBall ball = null;
		double[] v = normalize(vx, vy);

		try {
			Constructor<?> constructor = ballClass.getConstructors()[0];
			ball = (IBall) constructor.newInstance(cx, cy, width, height, color, speed, v[0], v[1]);
		} catch (Exception e) {

			System.out.println("Falha na instanciação da bola do tipo '" + ballClass.getName()
					+ "' ... Instanciando bola do tipo 'Ball'");
			ball = new Ball(cx, cy, width, height, color, speed, v[0], v[1]);
		}

		return ball;
	}

	/**
	 * Cria a bola principal do jogo. Este método é chamado pela classe Pong, que
	 * contem uma instância de BallManager.
	 * 
	 * @param cx     coordenada x da posição inicial da bola (centro do retangulo
	 *               que a representa).
	 * @param cy     coordenada y da posição inicial da bola (centro do retangulo
	 *               que a representa).
	 * @param width  largura do retangulo que representa a bola.
	 * @param height altura do retangulo que representa a bola.
	 * @param color  cor da bola.
	 * @param speed  velocidade da bola (em pixels por millisegundo).
	 * @param vx     componente x do vetor (não precisa ser unitário) que representa
	 *               a direção da bola.
	 * @param vy     componente y do vetor (não precisa ser unitário) que representa
	 *               a direção da bola.
	 */

	public void initMainBall(double cx, double cy, double width, double height, Color color, double speed, double vx,
			double vy) {

		theBall = createBallInstance(cx, cy, width, height, color, speed, vx, vy);
	}

	/**
	 * Método que desenha todas as bolas gerenciadas pela instância de BallManager.
	 * Chamado sempre que a(s) bola(s) precisa ser (re)desenhada(s).
	 */

	public void draw() {
		theBall.draw();
		try {
			balls.forEach(ball -> ball.draw());
		} catch (ConcurrentModificationException cme) {
			System.out.println("Esperando terminar de remover da lista...");
			new java.util.Timer().schedule(timeout(() -> balls.forEach(ball -> ball.draw())), 100);
		}
	}

	/**
	 * Método que atualiza todas as bolas gerenciadas pela instância de BallManager,
	 * em decorrência da passagem do tempo.
	 * 
	 * @param delta quantidade de millisegundos que se passou entre o ciclo anterior
	 *              de atualização do jogo e o atual.
	 */

	public void update(long delta) {
		theBall.update(delta);
		balls.forEach(ball -> ball.update(delta));
		// theBall.update(delta);
	}

	/**
	 * Método que processa as colisões entre as bolas gerenciadas pela instância de
	 * BallManager com uma parede.
	 * 
	 * @param wall referência para uma instância de Wall para a qual será verificada
	 *             a ocorrência de colisões.
	 * @return um valor int que indica quantas bolas colidiram com a parede (uma vez
	 *         que é possível que mais de uma bola tenha entrado em contato com a
	 *         parede ao mesmo tempo).
	 */

	public int checkCollision(Wall wall) {

		int hits = 0;

		for (IBall ball : balls) {
			if (ball.checkCollision(wall))
				hits++;
		}

		if (theBall.checkCollision(wall))
			hits++;

		return hits;
	}

	/**
	 * Método que processa as colisões entre as bolas gerenciadas pela instância de
	 * BallManager com um player.
	 * 
	 * @param player referência para uma instância de Player para a qual será
	 *               verificada a ocorrência de colisões.
	 */

	public void checkCollision(Player player) {

		theBall.checkCollision(player);
		balls.forEach(ball -> ball.checkCollision(player));
	}

	/**
	 * Método que processa as colisões entre as bolas gerenciadas pela instância de
	 * BallManager com um alvo.
	 * 
	 * @param target referência para uma instância de Target para a qual será
	 *               verificada a ocorrência de colisões.
	 */

	public void checkCollision(Target target) {
		targetCollisions(target, theBall);
		try {
			balls.forEach(ball -> targetCollisions(target, ball));
		} catch (ConcurrentModificationException cme) {
			new java.util.Timer().schedule(timeout(() -> balls.forEach(ball -> targetCollisions(target, ball))), 20);
		}
	}

	private void targetCollisions(Target target, IBall ball) {
		if (ball.checkCollision(target)) {
			switch (target.getClass().getName()) {
				case "BoostTarget":
					ball.setSpeed(BoostTarget.BOOST_FACTOR);
					new java.util.Timer().schedule(timeout(() -> ball.setSpeed(0.65)), BoostTarget.BOOST_DURATION);
					break;
				case "DuplicatorTarget":
					balls.add(createBallInstance(target.getCx(), target.getCy(), 20, 20, Color.RED, 0.65, ball.getVx(), ball.getVy()));
					CompletableFuture.runAsync(() -> new java.util.Timer().schedule(timeout(() -> cleanup()), DuplicatorTarget.EXTRA_BALL_DURATION));
					break;
			}
		}
	};

	private static TimerTask timeout(Runnable r) {
		return new TimerTask() {
			@Override
			public void run() {
				r.run();
			}
		};
	}

	private void cleanup() {
		IBall essaMinhaBola = balls.poll();
		essaMinhaBola = null;
	}

}
