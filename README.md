# Simulador de Elevador Inteligente

Este projeto implementa um simulador de elevadores inteligentes em um prédio, onde uma central de controle gerencia múltiplos elevadores. O sistema considera diversas variáveis como tempos de viagem, filas de espera, prioridades para pessoas com necessidades especiais e otimização de energia.

## Características

- Simulação de múltiplos elevadores em um prédio
- Três modelos de controle diferentes:
  1. Primeiro a chegar, primeiro a ser servido
  2. Otimização do tempo de espera
  3. Otimização do consumo de energia
- Suporte a prioridades para idosos e cadeirantes
- Simulação de horários de pico
- Cálculo de consumo de energia
- Interface de linha de comando interativa

## Requisitos

- Java 11 ou superior
- Maven

## Como Executar

1. Clone o repositório
2. Navegue até o diretório do projeto
3. Execute o comando Maven para compilar:
   ```
   mvn clean install
   ```
4. Execute o simulador:
   ```
   java -cp target/elevator-simulator-1.0-SNAPSHOT.jar com.elevator.ElevatorSimulator
   ```

## Uso

Ao iniciar o simulador, você será solicitado a configurar:
1. Número de andares (mínimo 5)
2. Número de elevadores
3. Capacidade de cada elevador
4. Modelo de controle desejado

O menu principal oferece as seguintes opções:
1. Adicionar pessoa
2. Processar requisições
3. Mostrar status
4. Alternar horário de pico
5. Sair

## Estrutura do Projeto

```
src/main/java/com/elevator/
├── ElevatorSimulator.java      # Classe principal
├── controller/
│   └── ElevatorController.java # Controlador dos elevadores
└── model/
    ├── Building.java          # Representa o prédio
    ├── Elevator.java          # Representa um elevador
    ├── Person.java            # Representa uma pessoa
    └── WaitingQueue.java      # Fila de espera personalizada
```

## Implementação

O projeto utiliza estruturas de dados personalizadas para gerenciar as filas de espera e o controle dos elevadores. As principais classes são:

- `WaitingQueue`: Implementa uma fila de espera personalizada
- `Person`: Representa uma pessoa no sistema
- `Elevator`: Gerencia o estado e movimento de um elevador
- `Building`: Coordena os elevadores e andares
- `ElevatorController`: Implementa as diferentes heurísticas de controle

## Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Crie um Pull Request 