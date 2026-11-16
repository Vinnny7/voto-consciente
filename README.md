# Voto Consciente

Simulador educativo de urna eletrônica desenvolvido para a disciplina de
Programação Orientada a Objetos, no curso de Ciência da Computação da Unijorge.

A urna usa Java Swing e candidatos fictícios. O participante vota primeiro para
deputado federal e depois para presidente. O simulador aceita votos em
candidatos, em branco e nulos. As duas escolhas são salvas em um único documento
no Cloud Firestore, sem coletar dados pessoais dos participantes.

## Organização do código

| Pacote | Classes | Responsabilidade |
| --- | --- | --- |
| `modelo` | `Candidato`, `SessaoVotacao` | Dados dos candidatos e regras da votação. |
| `persistencia` | `RepositorioVotos`, `RepositorioFirestore` | Operação de salvamento e integração com o Firestore. |
| `visao` | `TelaUrna` | Janela Swing, teclado, visor e interações. |

A sessão protege o estado da votação em atributos privados. A interface
`RepositorioVotos` define como a tela pede o salvamento. A tela usa essa
interface e o método executado vem da implementação fornecida, como
`RepositorioFirestore`. A classe `TelaUrna` herda de `JPanel`.

## Como executar no Windows

Instale o **JDK 21** e a **Google Cloud CLI**. Abra o PowerShell na pasta que
contém este README. Entre com uma conta que tenha acesso ao projeto
`voto-consciente-b1b46` no Firebase.

```powershell
gcloud.cmd auth application-default login
```

### Com o código do GitHub

```powershell
$arquivos = Get-ChildItem -Path .\src -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d .\classes $arquivos
java -cp .\classes visao.TelaUrna
```

### Com o JAR da oficina

Se você recebeu o ZIP com o executável, extraia o conteúdo e abra o PowerShell
na pasta que contém o JAR.

```powershell
java -jar .\VotoConsciente.jar
```

## Como testar

1. Com o computador conectado à internet, escolha `9202` para deputado federal e
   confirme. Para presidente, aperte **BRANCO** e depois **CONFIRMA**.
2. Ao aparecer **FIM**, abra **Firestore → Dados → votos** e confira o documento
   com `deputadoFederal: "9202"` e `presidente: "BRANCO"`.
3. Clique em **Nova votação** para começar outra sessão. Se o envio falhar, use
   **Tentar novamente** para reenviar a mesma votação.
