# Voto Consciente

O Voto Consciente é o simulador de urna eletrônica que fizemos para o trabalho de Programação Orientada a Objetos, do curso de Ciência da Computação da Unijorge. Durante o desenvolvimento, usamos como apoio o [tutorial do professor Nirton Afonso](https://nirtonafonso.github.io/Tutorial-Firebase/).

A urna usa Java Swing e Cloud Firestore. Cada participante vota para deputado
federal e depois para presidente, com candidatos e partidos fictícios. As quatro
fotos foram geradas por IA para estes personagens fictícios.

## O que o aplicativo faz

- Inicia com o teclado bloqueado.
- Abre a eleição com a senha didática do mesário, **99999**, e mostra a zerésima
  dos dois cargos com candidatos, votos brancos e votos nulos zerados.
- Mostra nome, partido e foto quando o número do cargo estiver completo.
- Permite corrigir a escolha e votar em candidato, em branco ou nulo.
- Grava os dois cargos juntos no Firestore. Se o envio falhar, o mesário pode
  reenviar a mesma votação com **Tentar novamente**.
- Encerra a eleição com **99999**, bloqueia a votação e abre a apuração em uma
  janela com uma aba por cargo, quantidades e percentuais.
- Executa operações de rede em segundo plano e informa as falhas na interface.

## Como abrir no Windows

1. Instale o **JDK 21** e a **Google Cloud CLI**.
2. Extraia o ZIP inteiro. Mantenha os arquivos na mesma pasta.
3. Abra um PowerShell novo e entre com a conta que tem acesso ao projeto Firebase.

   ```powershell
   gcloud.cmd auth application-default login
   ```

4. Se você recebeu o JAR pronto, dê dois cliques em **INICIAR.bat**. O comando
   equivalente, executado na pasta do JAR, é:

   ```powershell
   java -jar .\VotoConsciente.jar
   ```

5. Clique em **Abrir eleição**, informe **99999** e confira a zerésima.
   Feche apenas a janela da zerésima para começar as votações.

O notebook precisa de internet para abrir a eleição, salvar e consultar o
Firestore. Se houver falha durante o salvamento, mantenha a urna aberta e use
**Tentar novamente**. A tela **FIM** confirma o salvamento dos dois cargos.

## Configuração e autenticação

O arquivo **.env.example** contém o modelo de configuração. **INICIAR.bat**
cria a cópia local **.env** na primeira execução. Também é possível copiá-lo
manualmente.

```dotenv
FIREBASE_PROJECT_ID=voto-consciente-b1b46
FIREBASE_DATABASE_ID=(default)
```

O código consulta primeiro as variáveis do ambiente, depois o arquivo `.env`.
A autenticação aproveita as credenciais locais do **Google Cloud CLI** usadas
na versão anterior. O arquivo `.env` configura o projeto e o banco. As
credenciais de login são administradas pelo Google Cloud no perfil do usuário.
O `.gitignore` exclui a configuração local e arquivos de credenciais do Git.

## Como compilar o código do GitHub

No Windows, execute **COMPILAR.bat** com o JDK 21 instalado. Ele compila
o projeto, inclui a biblioteca Gson e as fotos e gera **VotoConsciente.jar**.

Para compilar e executar pelo PowerShell, use:

```powershell
javac -encoding UTF-8 -cp ".\lib\gson-2.11.0.jar" -d ".\classes" -sourcepath ".\src" ".\src\aplicacao\Main.java"
java -cp ".\classes;.\lib\gson-2.11.0.jar;.\recursos" aplicacao.Main
```

No Linux ou macOS:

```bash
javac -encoding UTF-8 -cp lib/gson-2.11.0.jar -d classes -sourcepath src src/aplicacao/Main.java
java -cp "classes:lib/gson-2.11.0.jar:recursos" aplicacao.Main
```

A dependência Gson 2.11.0 está em `lib`, para o projeto Java comum compilar
com as mesmas dependências em outros computadores.

## Candidatos fictícios

| Cargo | Número | Candidato | Partido |
| --- | --- | --- | --- |
| Deputado federal | 9101 | Ana do Ipê | Partido do Ipê |
| Deputado federal | 9202 | Lucas do Sol | Partido do Sol |
| Presidente | 91 | Marina do Ipê | Partido do Ipê |
| Presidente | 92 | Rafael do Sol | Partido do Sol |

## Organização MVC e POO

| Pacote | Responsabilidade |
| --- | --- |
| `aplicacao` | Montagem dos cargos, configuração do Firestore e início da aplicação. |
| `modelo` | Candidatos, cargos, sessão de votação, estados e cálculo do relatório. |
| `controle` | Abertura, confirmação, salvamento e encerramento da eleição. |
| `visao` | Telas Swing da urna e do relatório. |
| `persistencia` | Interface de repositório, proxy do Firestore e autenticação local. |
| `configuracao` | Leitura da configuração em `.env`. |
| `excecao` | Erros de regras da eleição e de acesso ao banco. |

- **Encapsulamento** — os atributos dos modelos são privados, e as operações
  controlam as mudanças na votação.
- **Abstração** — `Cargo` é uma classe abstrata. `RepositorioVotos` define as
  operações de persistência usadas pelo controlador.
- **Herança** — `CargoProporcional` e `CargoMajoritario` especializam `Cargo`.
  A tela da urna também herda de `JPanel`.
- **Polimorfismo** — o controlador trabalha com `RepositorioVotos`, que recebe
  uma implementação `FirestoreProxy`. Os cargos são tratados pelo tipo
  abstrato `Cargo`, com comportamento definido nas subclasses.

## Dados no Firestore

Cada abertura da aplicação prepara um identificador de eleição. Ao abrir a
eleição com a senha, o Firestore recebe um documento na coleção **eleicoes**.

As votações ficam em:

```text
eleicoes / ID_DA_ELEICAO / votos / ID_DA_VOTACAO
```

Cada documento de votação contém:

```json
{
  "deputadoFederal": "9202",
  "presidente": "BRANCO"
}
```

Os valores possíveis são um número de candidato cadastrado, `BRANCO` ou
`NULO`. A urna usa identificadores aleatórios e as escolhas simuladas. Cada
eleição tem seus próprios votos. A apuração lê somente os documentos da eleição
que foi aberta, com suporte a mais de uma página de resultados do Firestore.

O reenvio reutiliza o mesmo documento da votação. Ao encerrar, a urna grava
`estado: ENCERRADA` no documento da eleição e consulta os votos para montar
o relatório. Se houver falha nessa etapa, o encerramento permanece pendente e
o mesário pode tentar novamente.

As votações de teste das versões anteriores continuam na coleção `votos`
da raiz do banco. A apuração desta versão usa os registros de `eleicoes`.

## Roteiro de teste antes de entregar

1. Abra a urna e confira que o teclado está bloqueado.
2. Tente a senha `12345`. A eleição deve continuar bloqueada.
3. Abra com `99999`. A zerésima deve mostrar `0,00% (0 votos)` em todas as linhas.
4. Digite `920`. Depois complete com `2`. A foto e os dados de Lucas do Sol
   aparecem quando os quatro dígitos estiverem completos.
5. Aperte **CORRIGE** e confira que os dados e a foto sumiram.
6. Vote em `9202` para deputado e em **BRANCO** para presidente. Espere **FIM**.
7. Use **Nova votação**. Vote em `9101` para deputado e `99` para presidente.
8. Abra o Firebase e confira os dois documentos da eleição em
   **Firestore → Dados → eleicoes → ID da eleição → votos**.
9. Encerre com `99999`. O relatório deve mostrar dois votos por cargo,
   distinguindo os candidatos, brancos e nulos.
10. Confira que o teclado permanece bloqueado após o encerramento.
11. Abra uma nova instância do programa, abra e encerre uma eleição vazia.
    O relatório deve mostrar zero votos e zero por cento nos dois cargos.

## Versão web da oficina

A pasta `docs` contém a demonstração em HTML, CSS e JavaScript publicada pelo
GitHub Pages. Ela executa a votação no navegador, com as escolhas na memória
da página. O aplicativo Java/Swing realiza a persistência e a apuração no
Firestore.

## Material de apoio

- [Tutorial do professor Nirton Afonso](https://nirtonafonso.github.io/Tutorial-Firebase/)
- [API REST do Cloud Firestore](https://firebase.google.com/docs/firestore/use-rest-api)
- [Google Gson 2.11.0](https://github.com/google/gson/tree/gson-parent-2.11.0)
