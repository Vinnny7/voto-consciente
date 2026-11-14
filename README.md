# Voto Consciente

Simulador educativo de urna eletrônica desenvolvido para a disciplina de
Programação Orientada a Objetos, no curso de Ciência da Computação da Unijorge.

O simulador usa Java Swing e candidatos fictícios. A votação começa pelo cargo
de deputado federal e depois passa para presidente. A urna aceita votos em
candidatos, em branco e nulos. Ao final, salva as duas escolhas no Cloud
Firestore em um único registro, sem coletar dados pessoais dos participantes.

## Como executar no Windows

Instale o JDK 21 e a Google Cloud CLI. Abra o PowerShell na pasta dos arquivos
Java e faça login com uma conta que tenha acesso ao projeto no Firebase.

```powershell
gcloud.cmd auth application-default login
```

Depois, compile e execute o simulador:

```powershell
javac -encoding UTF-8 -d classes Candidato.java SessaoVotacao.java RepositorioVotos.java RepositorioFirestore.java TelaUrna.java
java -cp classes TelaUrna
```

## Como testar

Digite `9202` para deputado federal e confirme. Para presidente, aperte
**BRANCO** e depois **CONFIRMA**. Quando aparecer **FIM**, confira o registro
em **Firestore → Dados → votos**.
