# Compiladores‑T3

## Terceiro trabalho da disciplina de compiladores (T3) – Analisador Semântico para Linguagem LA

## Aluno

#### Yago David Pimenta • RA: 800273

## Professor

#### Andre Backes

### 📋 Descrição do Projeto

Este repositório contém a implementação do analisador semântico para a linguagem LA, desenvolvida com ANTLR4 e Java. O trabalho 3 (T3) consiste em detectar, sem interromper a análise, quatro tipos de erros semânticos:

Identificador já declarado no mesmo escopo (variável, constante, tipo, função ou procedimento).

Tipo não declarado na declaração de variáveis, constantes ou parâmetros.

Identificador não declarado em comandos e expressões.

Atribuição não compatível entre tipos (inteiro ↔ real, literal ↔ literal, lógico ↔ lógico, ponteiro, registros).

Ao fim da análise, todas as mensagens de erro são gravadas em um arquivo de saída, seguido de Fim da compilacao.

### 🛠️ Pré‑requisitos

Linux (ou Windows/Mac com bash)

Java 8+ (JDK instalado)

Maven (para compilação e empacotamento)

ANTLR4 Maven Plugin (já configurado no pom.xml)

Corretor automático (fornecido pelo professor)

Casos de teste (acesso via Google Classroom)

#### Instalação das Dependências

`sudo apt update
sudo apt install openjdk-11-jdk maven\`

#### 📥 Como Clonar

`git clone https://github.com/Yago-Pimenta/Compiladores-T3.git
cd Compiladores-T3`

#### 🚧 Compilação e Geração de JAR

`mvn clean generate-sources package`

Isso irá:

Gerar o código-fonte ANTLR4 em target/generated-sources/antlr4

Compilar todas as classes Java

Empacotar o JAR com dependências em target/MeuLexer-1.0-SNAPSHOT-jar-with-dependencies.jar

#### ▶️ Como Executar

`java -jar target/MeuLexer-1.0-SNAPSHOT-jar-with-dependencies.jar <entrada.la> <saida.txt>

<entrada.la>: caminho para o arquivo de teste (casos de erro sintático/sémântico).

<saida.txt>: caminho para gravar as mensagens de erro.` 

#### 🧪 Usando o Corretor Automático

Coloque o JAR do corretor automático (fornecido pelo professor) ao lado do projeto:

mv compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar .

Execute:

`java -jar compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar \
  "java -jar target/MeuLexer-1.0-SNAPSHOT-jar-with-dependencies.jar" \
  gcc temp casos-de-teste "800273" t3`

Substitua 800273 pelo seu RA.

A saída do corretor exibirá quantos testes passaram.

📄 Casos de Teste

Os arquivos de entrada e saída de referência estão disponíveis na pasta casos-de-teste (precisa baixar do Classroom).

🎯 Pontuação Esperada

O analisador semântico deve detectar corretamente todos os erros solicitados e garantir 7/9 de acerto nos casos de teste do T3.

Atenciosamente,

#### Yago David Pimenta


