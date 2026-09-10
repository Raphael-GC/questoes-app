# Questões

Aplicativo Android de autoavaliação para concursos públicos e processos seletivos na área de Geografia e educação, com um banco de mais de 570 questões organizadas por disciplina, tags de assunto e concurso de origem.

O projeto nasceu de uma necessidade pessoal de estudo para concursos (PSS SEDUC-SP, processos seletivos municipais, ENADE de Licenciatura em Geografia) e evoluiu para um banco de questões estruturado, com o objetivo de treinar por disciplina, por tema específico ou simulando a prova real de um cargo.

## Status

Em desenvolvimento ativo. O banco de dados local (Room) já está implementado e populado com as 574 questões; as telas de uso (Compose) ainda estão por vir — ver [Roadmap](#roadmap).

## Funcionalidades planejadas

- **Modo Livre**: escolha uma ou mais disciplinas, filtre por tags de assunto e defina a quantidade de questões.
- **Modo Simulado**: prova completa de um cargo específico, respeitando a proporção de questões por área definida no edital.
- **Correção com feedback**: explicação de cada questão logo após a resposta, com cronômetro por questão e por sessão.
- **Histórico local**: todas as tentativas ficam salvas no aparelho, com revisão por sessão.
- **Atualização do banco sem reinstalar o app**: novas questões e imagens são publicadas em repositórios próprios no GitHub e sincronizadas pelo app.

## Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose |
| Persistência local | Room (SQLite) |
| Build | Android Gradle Plugin 9, Kotlin embutido do AGP (sem plugin Kotlin clássico) |
| Serialização | kotlinx.serialization |
| Anotações/codegen | KSP |
| Imagens remotas | Coil (carregamento sob demanda) |

`minSdk` 24 (Android 7.0), `compileSdk`/`targetSdk` 37.

## Arquitetura

O app segue uma separação simples por responsabilidade, em módulo único:

```
net.oraphael.questoes
├── data
│   ├── db          → entidades Room, DAO e AppDatabase
│   └── importer     → converte o JSON do banco de questões em linhas do Room
├── ui
│   └── theme        → cores, tipografia e tema Compose
├── MainActivity.kt
└── QuestoesApplication.kt
```

**Modelo de dados.** As questões são distribuídas como arquivos `.json` por disciplina (em `app/src/main/assets/questoes/`) e importadas para um banco Room local na primeira execução do app. Esse banco é a única fonte consultada em tempo de execução — os `.json` nunca são lidos diretamente pela UI. O importador normaliza uma inconsistência histórica do banco de questões, em que o campo `alternativas` aparece ora como dicionário (`{"A": "texto", ...}`), ora como lista de objetos (`[{"letra": "A", "texto": "..."}]`), unificando os dois formatos numa única tabela relacional (`Alternativa`).

**Imagens.** Questões que dependem de imagem não a incluem no `.json` — apenas um indicador (`possui_imagem`) e uma descrição textual. A URL da imagem é montada em tempo de execução a partir do id da questão e resolvida contra um repositório dedicado no GitHub ([`questoes-banco`](https://github.com/Raphael-GC/questoes-banco)), servido via `raw.githubusercontent.com` e carregado sob demanda — sem empacotar imagens dentro do `.apk` nem recompilar o app a cada imagem nova.

**Atualização remota do banco.** A atualização de questões sem exigir uma nova versão do app é resolvida via um `manifest.json` versionado, também servido por GitHub raw: o app compara a versão local com a remota, baixa apenas os arquivos de disciplina que mudaram e reaproveita o mesmo importador — sem nunca tocar no histórico de tentativas do usuário.

## Banco de questões

| Disciplina | Questões |
|---|---|
| Gerais / Didático-pedagógico | 254 |
| Geografia | 160 |
| Português | 108 |
| Educação Infantil | 44 |
| Educação Especial | 8 |
| **Total** | **574** |

As questões vêm de provas oficiais reais (PSS SEDUC-SP, ENADE Licenciaturas Geografia/INEP-Sinaes, e diversos processos seletivos municipais) e de um conjunto de questões autorais escritas com base em editais e material de estudo, para cobrir cargos e conteúdos ainda sem prova pública disponível. Cada questão é autocontida (enunciado completo, sem depender de contexto externo) e traz alternativas, resposta correta, explicação, tags de assunto e a referência do concurso de origem.

O código-fonte do banco de questões (os arquivos `.json`) vive em um repositório próprio, separado deste; este repositório contém apenas o aplicativo.

## Como rodar o projeto

1. Clone o repositório e abra a pasta no Android Studio.
2. Deixe o Gradle sincronizar (o projeto usa o catálogo de versões em `gradle/libs.versions.toml` — nenhuma configuração manual é necessária).
3. Rode o app em um emulador ou dispositivo com Android 7.0 ou superior.
4. Na primeira execução, o app importa as 574 questões dos arquivos em `assets/questoes/` para o banco Room local — isso acontece uma única vez, em segundo plano.

## Roadmap

- [x] Banco de questões consolidado e validado (574 questões, 5 disciplinas)
- [x] UX definida (mapa de navegação, fluxos de Livre/Simulado/Histórico)
- [x] Direção visual e catálogo de componentes
- [x] Arquitetura técnica definida e documentada
- [x] Camada de dados: schema Room, importador de JSON, importação automática no primeiro boot
- [ ] Telas do MVP em Compose (Home, Seleção, Quiz, Resultado, Histórico, Simulado, Atualização de banco)
- [ ] Pós-MVP: revisão por tag, gráfico de evolução, exportação/backup do histórico
- [ ] Testes com uso real e ajustes de UX
- [ ] Build final (APK)

## Licença

Distribuído sob a [GNU General Public License v3.0](LICENSE).

## Autor

Raphael Guedes Carneiro — [github.com/Raphael-GC](https://github.com/Raphael-GC)
