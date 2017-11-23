package competition.icegic.perez;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class Perez implements Agent
{
    private boolean[] Action = null;
    private String Name = "MarioPerez_Perez";
    private byte[][] terreno;
    private byte[][] todoTerreno;
    private Environment observation;

    public Perez()
    {
        this.reset ();
//        RegisterableAgent.registerAgent(this);
    }

    private final int GRID_SIZE = 22;
    private final int MARIO_CENTER = 11;
    private final int EAST_CELL = MARIO_CENTER + 1;
    private final int SOUTH_CELL = MARIO_CENTER + 1;

    private boolean pozoDelante() {
        for (int position = SOUTH_CELL; position < GRID_SIZE; position++) {
            if (isSuelo(position, EAST_CELL)) {
                return false;
            }
        }
        return true;
    }

    float[] enemigoSaltado;

    private void localizarEnemigoSaltado() {
        int leido = 0;
        float[] valorLeido = new float[3];
        float[] masCercanoEncontrado = new float[3];
        double distanciaMasCercano = Double.MAX_VALUE;
        for (float valor : observation.getEnemiesFloatPos()) {
            valorLeido[leido] = valor;
            if (leido == 2) {
                double distancia = Math.sqrt(
                        Math.pow(valorLeido[1] - observation.getMarioFloatPos()[0], 2) +
                        Math.pow(valorLeido[2] - observation.getMarioFloatPos()[1], 2)
                        );
                if (distancia < distanciaMasCercano && observation.getMarioFloatPos()[0] < valorLeido[1] && isEnemigoSaltable((int) valorLeido[0])) {
                    distanciaMasCercano = distancia;
                    masCercanoEncontrado = valorLeido.clone();
                }
                leido = 0;
            } else {
                leido++;
            }
        }

        enemigoSaltado = masCercanoEncontrado;
    }

    private void seguirEnemigoSaltado() {
        int leido = 0;
        float[] valorLeido = new float[3];
        float[] masCercanoEncontrado = new float[3];
        double distanciaMasCercano = Double.MAX_VALUE;
        for (float valor : observation.getEnemiesFloatPos()) {
            valorLeido[leido] = valor;
            if (leido == 2) {
                double distancia = Math.sqrt(
                        Math.pow(valorLeido[1] - enemigoSaltado[1], 2) +
                        Math.pow(valorLeido[2] - enemigoSaltado[2], 2)
                        );
                if (distancia < distanciaMasCercano && observation.getMarioFloatPos()[1] < valorLeido[1]) {
                    distanciaMasCercano = distancia;
                    masCercanoEncontrado = valorLeido.clone();
                }
                leido = 0;
            } else {
                leido++;
            }
        }

        enemigoSaltado = masCercanoEncontrado;
    }

    private boolean recamaraVacia() {
        int balas = 0;
        for (int i = 0; i < 22; i++) {
            for (int ii = 0; ii < 22; ii++) {
                if (todoTerreno[i][ii] == 25) {
                    balas++;
                }
            }
            if (balas > 1) {
                return true;
            }
        }
        return false;
    }

    private boolean enemigoATiro() {
        if (observation.getMarioMode() < 2) {
            return false;
        }

        if (recamaraVacia()) {
            return false;
        }

        int leido = 0;
        float[] valorLeido = new float[3];
        for (float valor : observation.getEnemiesFloatPos()) {
            valorLeido[leido] = valor;
            if (leido == 2) {
                float distancia = valorLeido[1] - observation.getMarioFloatPos()[0];
                float altura = valorLeido[2] - observation.getMarioFloatPos()[1];
                if (
                        distancia > 0 && distancia < 45 &&
                        altura > -25 && altura < 25 &&
                        isEnemigoDisparable((int) valorLeido[0])) {
                    return true;
                }
                leido = 0;
            } else {
                leido++;
            }
        }
        return false;
    }

    private boolean enemigoSaltableDelante() {
        int leido = 0;
        float[] valorLeido = new float[3];
        for (float valor : observation.getEnemiesFloatPos()) {
            valorLeido[leido] = valor;
            if (leido == 2) {
                float distancia = valorLeido[1] - observation.getMarioFloatPos()[0];
                float altura = valorLeido[2] - observation.getMarioFloatPos()[1];
                if (distancia > 5 && distancia < 26 && isEnemigoSaltable((int) valorLeido[0]) &&
                        altura > -5 && altura < 15) {
                    return true;
                }
                leido = 0;
            } else {
                leido++;
            }
        }
        return false;
    }

    private boolean paredDelante() {
        return (isSuelo(MARIO_CENTER, EAST_CELL));
    }

    private boolean piedraDelante() {
        return (isPiedra(terreno[MARIO_CENTER][EAST_CELL]));
    }

    private boolean isSuelo(int x, int y) {
        int cosa = terreno[x][y];
        int todaCosa = todoTerreno[x][y];
        return (cosa == -10 || cosa == 20 || cosa == 46 || todaCosa == 14);// || todaCosa == -1);
    }

    private boolean isEnemigoDisparable(int cosa) {
        return (cosa == 2 || cosa == 6 || cosa == 4 || cosa == 12);
    }

    private boolean isEnemigoSaltable(int cosa) {
        if (observation.getMarioMode() < 2 || recamaraVacia()) {
            return true;
        } else {
            return (cosa == 9 || cosa == 8);
        }
    }

    private boolean isPiedra(int cosa) {
        return (cosa == -12);
    }

    public void reset()
    {
        //GlobalOptions.pauseWorld = true;
        Action = new boolean[10];
    }

    public boolean[] getAction(Environment observation)
    {
        assert(observation != null);
        this.observation = observation;
        terreno = observation.getLevelSceneObservation();
        todoTerreno = observation.getCompleteObservation();

//        for (int i = 0; i < 22; i++) {
//            for (int ii = 0; ii < 22; ii++) {
//                String thing = new Byte(enemigos[i][ii]).toString();
//                thing += thing.length() < 4 ? "X" : "";
//                thing += thing.length() < 4 ? "X" : "";
//                thing += thing.length() < 4 ? "X" : "";
//                System.out.print(thing);
//                System.out.print(ii < 21 ? ":" : "");
//            }
//            System.out.println("");
//        }

        avanzar();
        saltarParedes();
        subirEscaleras();
        saltarPozos();
        saltarEnemigos();
        dispararEnemigos();
        evitarArrollarEnemigos();
        return Action;
    }

    private int ciclosSaltoPiedra;
    private int ciclosSaltoPozos;
    private int ciclosSaltoParedes;
    private int estadoSaltoEnemigos;
    private float ultimaPosicionMario;

    private void avanzar() {
        Action[Mario.KEY_SPEED] = false;
        Action[Mario.KEY_LEFT] = false;
        Action[Mario.KEY_RIGHT] = !pozoDelante() && !piedraDelante() && observation.mayMarioJump();
        Action[Mario.KEY_JUMP] = paredDelante() && observation.mayMarioJump();
        if (observation.getMarioFloatPos()[0] > ultimaPosicionMario + 4) {
            Action[Mario.KEY_RIGHT] = false;
        }
        ultimaPosicionMario = observation.getMarioFloatPos()[0];
    }

    private int ultimoDisparo = 0;

    private void dispararEnemigos() {
        if (ultimoDisparo == 0) {
            if (enemigoATiro()) {
                Action[Mario.KEY_SPEED] = true;
                Action[Mario.KEY_RIGHT] = true;
                ultimoDisparo = 1;
            }
        } else {
            ultimoDisparo = ultimoDisparo > 1 ? 0 : ultimoDisparo + 1;
        }
    }

    private void evitarArrollarEnemigos() {
        if (!observation.mayMarioJump()) {
            int leido = 0;
            float[] valorLeido = new float[3];
            for (float valor : observation.getEnemiesFloatPos()) {
                valorLeido[leido] = valor;
                if (leido == 2) {
                    float distancia = valorLeido[1] - observation.getMarioFloatPos()[0];
                    float altura = valorLeido[2] - observation.getMarioFloatPos()[1];
                    if (distancia > 0 && distancia < 35 &&
                            altura > -35 && altura < 25 &&
                            estadoSaltoEnemigos == 0 && ultimoDisparo != 1) {
                        Action[Mario.KEY_LEFT] = true;
                        Action[Mario.KEY_RIGHT] = false;
                    }
                    leido = 0;
                } else {
                    leido++;
                }
            }
        }
    }

    private void saltarEnemigos() {
        if (estadoSaltoEnemigos > 0) {
            seguirEnemigoSaltado();
            float xEnemigo = enemigoSaltado[1];
            float xMario = observation.getMarioFloatPos()[0];
            Action[Mario.KEY_RIGHT] = true;
            Action[Mario.KEY_JUMP] = true;
            if (estadoSaltoEnemigos == 1 && xEnemigo < xMario) {
                estadoSaltoEnemigos = 2;
            }
            if (estadoSaltoEnemigos == 2) {
                if (xEnemigo < xMario - 15f) {
                    Action[Mario.KEY_RIGHT] = false;
                    Action[Mario.KEY_LEFT] = true;
                } else {
                    Action[Mario.KEY_RIGHT] = true;
                    Action[Mario.KEY_LEFT] = false;
                }
            }
            if (observation.isMarioOnGround()) {
                Action[Mario.KEY_RIGHT] = true;
                Action[Mario.KEY_LEFT] = false;
                estadoSaltoEnemigos = 0;
            }
        } else {
            if (enemigoSaltableDelante() && observation.mayMarioJump()) {
                localizarEnemigoSaltado();
                Action[Mario.KEY_RIGHT] = true;
                Action[Mario.KEY_JUMP] = true;
                Action[Mario.KEY_SPEED] = false;
                estadoSaltoEnemigos = 1;
            }
        }
    }

    private void saltarParedes() {
        if (ciclosSaltoParedes > 0) {
            Action[Mario.KEY_RIGHT] = true;
            Action[Mario.KEY_JUMP] = true;
            ciclosSaltoParedes++;
            int paso1 = 3;
            int paso2 = paso1 + 10;
            int paso3 = paso2 + 3;
            int paso4 = paso3 + 3;
            if (ciclosSaltoParedes > paso1) {
                Action[Mario.KEY_RIGHT] = false;
                Action[Mario.KEY_SPEED] = false;
                Action[Mario.KEY_JUMP] = true;
                if (!paredDelante()) {
                    ciclosSaltoParedes = paso4 + 1;
                }
                if (ciclosSaltoParedes > paso2) {
                    Action[Mario.KEY_RIGHT] = true;
                    Action[Mario.KEY_SPEED] = false;
                    Action[Mario.KEY_JUMP] = false;
                    if (ciclosSaltoParedes > paso3) {
                        if (ciclosSaltoParedes > paso4) {
                            ciclosSaltoParedes = 0;
                        }
                    }
                }
            }
        } else {
            if (paredDelante() && observation.mayMarioJump()) {
                Action[Mario.KEY_RIGHT] = true;
                Action[Mario.KEY_JUMP] = true;
                ciclosSaltoParedes = 1;
            }
        }
    }

    private void saltarPozos() {
        if (ciclosSaltoPozos > 0) {
            Action[Mario.KEY_RIGHT] = true;
            Action[Mario.KEY_JUMP] = false;

            ciclosSaltoPozos++;
            int paso1 = 2;
            int paso2 = paso1 + 10;
            int paso3 = paso2 + 5;
            int paso4 = paso3 + 5;
            if (ciclosSaltoPozos > paso1) {
                Action[Mario.KEY_RIGHT] = true;
                Action[Mario.KEY_JUMP] = true;
                if (!pozoDelante() && ciclosSaltoPozos < paso2) {
                    ciclosSaltoPozos = paso2 + 1;
                }
                if (ciclosSaltoPozos > paso2) {
                    Action[Mario.KEY_RIGHT] = true;
                    Action[Mario.KEY_JUMP] = false;
                    if (ciclosSaltoPozos > paso3) {
                        if (ciclosSaltoPozos > paso4) {
                            ciclosSaltoPozos = 0;
                        }
                    }
                }
            }
        } else {
            if (pozoDelante() && observation.mayMarioJump()) {
                Action[Mario.KEY_RIGHT] = true;
                Action[Mario.KEY_JUMP] = false;
                ciclosSaltoPozos = 1;
            }
        }
    }

    private void subirEscaleras() {
        if (ciclosSaltoPiedra > 0) {
            Action[Mario.KEY_RIGHT] = true;
            Action[Mario.KEY_JUMP] = false;

            ciclosSaltoPiedra++;
            int paso1 = 5;
            int paso2 = paso1 + 1;
            int paso3 = paso2 + 1;
            int paso4 = paso3 + 1;
            if (ciclosSaltoPiedra > paso1) {
                Action[Mario.KEY_RIGHT] = false;
                Action[Mario.KEY_JUMP] = true;
                if (ciclosSaltoPiedra > paso2) {
                    Action[Mario.KEY_RIGHT] = true;
                    Action[Mario.KEY_JUMP] = false;
                    if (ciclosSaltoPiedra > paso3) {
                        Action[Mario.KEY_RIGHT] = false;
                        Action[Mario.KEY_JUMP] = false;
                        if (ciclosSaltoPiedra > paso4) {
                            ciclosSaltoPiedra = 0;
                        }
                    }
                }
            }
        } else {
            if (piedraDelante() && observation.mayMarioJump()) {
                Action[Mario.KEY_RIGHT] = true;
                Action[Mario.KEY_JUMP] = false;
                ciclosSaltoPiedra = 1;
            }
        }
    }

    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

}
