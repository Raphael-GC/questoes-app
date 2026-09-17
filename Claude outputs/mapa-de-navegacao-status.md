# Questões — Mapa de Navegação (conteúdo real do artefato, exportado em 14/09/2026)

> Artefato original: https://claude.ai/code/artifact/e2929deb-07eb-431e-90d9-4054f36b4931 — link não é acessível por sessões sem login do dono (retorna só a casca JS), por isso este export em texto puro.

## Status geral (stepper do roadmap)

1. Banco de dados — ✅ concluída
2. UX — ✅ concluída
3. UI — ✅ concluída
4. Arquitetura — ✅ concluída
5. **MVP — 🔶 em andamento (fase ativa agora)**
6. Pós-MVP — não iniciada
7. Testes — não iniciada
8. Build final — não iniciada

## Nota de atualização mais recente (14/09/2026)

Fases 3 (UI) e 4 (Arquitetura) fecharam nos dias seguintes à aprovação da Fase 2 (06/09), cada uma no seu próprio artefato (Direções Visuais e Protótipo em 07/09, Catálogo de Componentes "Carta anotada" em 08/09, Arquitetura do Questões em 09/09).

A partir daí entrou-se em **Fase 5 — MVP**:
- Banco Room populado com as 574 questões (schema de `questão`/`alternativa`/`tag` em 09/09, depois `sessão`/`tentativa` em 12/09 para registrar as métricas de uso).
- Navegação com as 7 rotas type-safe deste mapa implementada (Navigation Compose clássico, depois de um crash sem diagnóstico fechado no Navigation 3, que foi abandonado).
- Tema visual "Carta anotada" implementado em Compose (paleta, tipografia, raios de borda).
- **Tela 1 (Home)** virou a primeira tela real do app, substituindo o placeholder — as outras seis seguem como placeholder até a etapa de cada uma.

*Nota de precisão (não está no artefato): o código da Home compila, mas ainda não foi confirmado rodando no emulador com screenshot + Logcat — esse é o próximo passo antes de dar a etapa como 100% fechada.*

## Status por tela (§3 — sete telas do fluxo, mais o pop-up de tags)

| Tela | Nome | Status |
|---|---|---|
| 1 | Home | **implementada** (aguardando confirmação final em emulador) |
| 2 | Seleção · Livre | placeholder |
| 2 · pop-up | Tags da disciplina | placeholder |
| 3 | Quiz | placeholder |
| 4 | Resultado | placeholder |
| 5 | Histórico geral | placeholder |
| 6 | Seleção de simulado | placeholder |
| 7 (nova) | Atualizar banco de questões | placeholder |

Ordem planejada das próximas etapas: Tela 2 (Seleção · Livre + pop-up de tags) → Quiz → Resultado → Histórico → Seleção de simulado (precisa antes das tabelas Concurso/Simulado/Bloco + import de `simulados.json`) → Atualizar banco.

## Fluxo de uso (§1, resumo)

Uma sessão de estudo é um laço: cada pergunta gera feedback imediato antes da próxima, e só a última pergunta abre caminho para o resumo. **Livre** (múltiplas disciplinas, tags via pop-up obrigatório, ordem aleatório/sequencial) e **Simulados** (cargo → 3 blocos por peso, sorteio por pool) são as duas portas de entrada a partir da Home — as duas caem no mesmo laço Pergunta→Feedback e no mesmo Resumo. O Resumo não é exclusivo do fim de sessão: o Histórico reabre o mesmo Resumo de qualquer sessão passada.

## Modo Livre — decisões de design fechadas (§4)

- **Combinação de tags:** qualquer uma (OR), por disciplina — não todas ao mesmo tempo (AND).
- **Escopo da busca:** múltiplas disciplinas na mesma sessão, um pop-up de tags por disciplina.
- **Ordem:** Aleatório intercala questões de todas as disciplinas marcadas; Sequencial agrupa, uma disciplina inteira por vez.
- **Estado inicial do pop-up:** sempre vazio (tags desmarcadas), nunca "todas" por padrão; Voltar sempre ativo, Confirmar só libera com 1+ tag marcada.
- **Quantidade maior que o pool disponível:** bloqueia e pede ajuste, checagem por disciplina.
- **O que substitui o antigo modo "Erros":** exibição das tags das questões erradas (agrupadas por disciplina, sem repetição) — não é mais uma sessão nova.

## Simulados de cargo — decisões de design fechadas (§5)

- **Rotação:** sorteio por pool a cada tentativa (não é uma lista fixa de 40 ids).
- **Composição da prova:** 10 (Língua Portuguesa, peso 1,0) + 15 (Pedagógicos/Legislação, peso 2,0) + 15 (Conhecimentos Específicos, peso 4,0) = 40 questões, mesma estrutura para os 3 cargos do Processo Seletivo 02/2026 (Berçarista, Professor I, Professor II-Geografia).
- **Pool menor que a quantidade exigida:** mesma regra de bloqueio do modo Livre — nunca sorteia silenciosamente menos questões que o edital pede.
- Ainda não implementado no Room: falta as tabelas `Concurso`/`Simulado`/`BlocoSimulado`/`BlocoPoolCrossRef` e o import de `simulados.json`.

## Rodapé do artefato

> Fase 2 aprovada em 06/09/2026 — fluxo, métricas e as sete telas fechados. Fases 3 (UI) e 4 (Arquitetura) fecharam em 08/09 e 09/09, cada uma no seu próprio artefato.
> **EM ANDAMENTO · Fase 5 — MVP · Home implementada (14/09) — próxima: Tela 2, Seleção · Livre**
