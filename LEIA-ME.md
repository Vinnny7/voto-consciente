# Voto Consciente com Firestore

Simulador educativo em Java Swing. A urna registra os dois votos simulados
no projeto Firebase `voto-consciente-b1b46`, na coleção `votos`. Cada
documento tem um identificador aleatório e os campos `deputadoFederal` e
`presidente`. Os valores são números de candidatos fictícios, `BRANCO`
ou `NULO`.

## Como abrir no Windows

1. Extraia o ZIP inteiro. Abra a pasta que contém `VotoConsciente.jar`.
2. Instale o Java 21 e a [CLI do Google Cloud para Windows](https://docs.cloud.google.com/sdk/docs/downloads-interactive?hl=pt-br).
3. Abra um PowerShell novo nessa pasta. Na primeira vez, execute o comando
   abaixo. Entre com a conta Google que tem acesso ao projeto do Firebase.

   ```powershell
   gcloud.cmd auth application-default login
   ```

4. Na mesma pasta, abra a urna com o comando abaixo.

   ```powershell
   java -jar .\VotoConsciente.jar
   ```

5. Para testar, vote em `9202` para deputado federal e confirme `BRANCO`
   para presidente. Depois de aparecer `FIM`, abra o Firebase em
   **Firestore → Dados → votos** e confira o documento.

A urna usa a sessão local do Google Cloud. O login fica no computador de
quem está conduzindo a oficina. Os alunos só precisam usar os botões.
Se o envio falhar, clique em **Tentar novamente**. Deixe a janela aberta
até aparecer **FIM**, que confirma que o Firestore recebeu os dois votos.

## Como compilar o código

Abra o PowerShell na pasta dos arquivos Java e use o compilador do JDK 21.

```powershell
javac -encoding UTF-8 -d classes Candidato.java SessaoVotacao.java RepositorioVotos.java RepositorioFirestore.java TelaUrna.java
java -cp classes TelaUrna
```
