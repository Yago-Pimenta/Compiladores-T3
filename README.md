# CompiladoresT2
Segundo trabalho da disciplina de compiladores - Analisador Léxico com um parser léxico 
## Aluno : Yago David Pimenta RA : 800273
## Professor : Andre Backes
### Requisitos necessários : 
- Linux
- Java
- gcc
- Instalar o coretor automático
- Baixar os casos de teste

  ### Como baixar os requisitos
  ```sudo apt install openjdk-11-jre-headless```

  ```sudo apt install gcc```

  - Para baixar o corretor automático deve-se abrir o repositório indicado pelo professor e seguir seus passos : https://github.com/dlucredio/compiladores-corretor-automatico/blob/master/target/compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar
 
  - Para baixar os casos de testes , é necessário que o aluno tenha acesso ao classroom da matéria e seguir o seuginte link https://classroom.google.com/u/1/c/NzYyMzQ4NjY2MTg5/m/NzYyMzQ4NjY2MjMy/details

 ### Como rodar o código 
 Agora que já finalizamos todas as etapas de pré requistos podemos partir para como rodar o meu código
 
- Vamos clonar meu repositório
 ```git clone https://github.com/Yago-Pimenta/CompiladoresT2.git```

 - Precisamos compilar o projeto : abrir a pasta do t2 no terminal e digitar
    
   ```mvn clean```
   ```mvn generate-sources```
   ```mvn package```

### Agora vamos rodar o corretor automático com os casos de teste 
- Vamos criar um diretório específico para ele ao lado do meu t2 , finalmente podemos rodar meu corretor

  ```java -jar compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar "java -jar ../MeuLexico/target/MeuLexer-1.0-SNAPSHOT-jar-with-dependencies.jar" gcc temp casos-de-teste "800273" t2```
- Lembrando que o 800273 deve ser substituído pelo RA respectivo do aluno

- Eu não consegui tirar 62 , minha nota ficou 60/62

- Qualquer dúvida basta me mandar uma mensagem - Atenciosamente : Yago


# Compiladores-T3
