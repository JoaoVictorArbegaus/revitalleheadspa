# Direção visual - Revitalle Head Spa

> Contrato das Etapas 2 (código), 3 (acabamento) e 4 (verificação). Documento único: ao revisar, reescrever o arquivo inteiro.
> **Revisão 2** (após reprovação do usuário): a referência `referencias/6a0ea2a682728ccffd2bcba5f9fb678c.png` passa a ser o modelo de composição principal, com a paleta e o logo do brand kit, e o **liquid glass é a linguagem visual dominante**.

## Direção escolhida

**Opção C - Lente Âmbar, revisada.** Página clara e quente com capítulos escuros, agora composta como o mock de referência:

- hero com **foto do ambiente em tela cheia, nítida e reconhecível**, texto centralizado aplicado direto sobre a foto, sem painel em volta;
- **pílula de navegação de vidro** flutuando sobre a foto;
- **três cartões de vidro sobrepostos à borda inferior do hero** (ícone de linha fino + título + texto curto);
- serviços logo abaixo, com título centralizado.

Pedidos do usuário que regem esta revisão:

1. Logo como `<img>` real (versões limpas em dourado e marrom); nada de `mask-image` para conteúdo essencial. A página precisa funcionar aberta por `file://` e por `http`.
2. Nada de fundos de foto superdesfocados. No hero, foto nítida (no máximo 1px de suavização na origem), compensada por degradê quente escuro e grão leve.
3. Nenhum painel em volta do h1. Vidro em **elementos**, não em caixas que embrulham texto corrido (ver "Liquid glass").
4. Fotos em retângulos de cantos levemente arredondados (sem arco).
5. Faixa de 3 cartões de vidro sobre a base do hero, com os pilares do briefing.
6. Sem círculos dourados numerados; numeração tipográfica discreta e linha do tempo fina. Seções compactas, sem colunas vazias.
7. Nenhuma emenda dura entre seções: degradês longos entre cores da paleta, sobreposição de elementos e fundo contínuo. Fundos escuros em espresso/noz com degradê e manchas de luz, não foto borrada.
8. Mais animação e fluidez (lista em "Movimento"), sempre com `prefers-reduced-motion`.
9. Troca da Cormorant Garamond por outra serifada (comparação em "Tipografia").
10. **Glass dominante:** pílula, botões, selos no hero, cartões dos pilares, placas de serviço, depoimentos, legendas de foto, bloco de contato e horários, WhatsApp flutuante e menu móvel em vidro. **A regra de 3 a 5 blocos de vidro por tela fica suspensa por pedido explícito do usuário**; o contraste AA continua obrigatório.

**Lente WebGL: removida.** Sobre a foto nítida, ampliada para tela cheia a partir de 876px, a lente ampliaria a pixelização, brigaria com o parallax e com o vidro da pílula e dos selos, e pesaria mais que o ganho. O "líquido" do hero passa para o vidro (refração SVG, reflexo que segue o ponteiro, distorção ao hover) e para o parallax da foto.

## Paleta

Base: brand kit. Derivados marcados com (d).

| Token | Hex | Papel |
| --- | --- | --- |
| `--espresso` | `#23140F` | texto principal no claro; base dos capítulos escuros |
| `--noz` | `#4A2C1B` | texto secundário no claro; degradês e manchas no escuro |
| `--ouro` | `#C8A062` | vidro do botão principal, bordas especulares, manchas de luz, ícones. **Nunca texto em fundo claro** |
| `--ouro-claro` | `#E2C693` | rótulos e destaques no escuro; brilho de borda; foco no escuro |
| `--areia` | `#EDE3D4` | fundo de leitura; texto principal no escuro |
| `--creme` (d) | `#F7F2EA` | fundo de leitura principal; tinta do vidro claro |
| `--ouro-texto` (d) | `#634820` | rótulos e números no claro |
| `--areia-2` (d) | `#CDBBA5` | texto secundário no escuro |
| `--verde-wa` (d) | `#1F6B47` | tinta do vidro do WhatsApp flutuante |

| Texto / fundo | Contraste |
| --- | --- |
| Espresso / creme | 16,0:1 |
| Espresso / areia | 14,0:1 |
| Noz / creme | 11,3:1 |
| Ouro-texto / areia | 6,6:1 |
| Espresso / ouro | 7,4:1 |
| Areia / espresso | 14,0:1 |
| Areia-2 / espresso | 9,5:1 |
| Ouro-claro / espresso | 10,8:1 |
| Ouro / areia | 1,9:1 (**proibido como texto**) |

Contraste sobre vidro e foto é medido no pior ponto do fundo composto:

- **Vidro claro** (tinta creme ≥ 0,5) sobre mancha de ouro: espresso ≥ 12:1, noz ≥ 8:1, ouro-texto ≥ 4,5:1.
- **Vidro escuro** (tinta espresso ≥ 0,38) sobre mancha âmbar: areia ≥ 7:1, ouro-claro ≥ 5,5:1.
- **Botão de vidro dourado** (tinta ouro 0,8) sobre fundo escuro: espresso ≈ 5:1.
- **Texto do hero:** medido no navegador sobre a foto real com o degradê aplicado (registrado na Etapa 4).

## Tipografia

Comparação renderizada (Cormorant Garamond, Playfair Display, Bodoni Moda, Gilda Display, Marcellus, Libre Caslon Display, Prata) com "Seu momento de se reconectar com você" e "experiência", em `scratchpad/fontcmp/comparacao-fontes.png`.

- Cormorant: o circunflexo fica solto.
- Bodoni Moda: renderização quebrada pelo eixo óptico.
- Marcellus: ecoa o "REVITALLE" do logo, mas não tem itálico e fica sem contraste no corpo do título.
- **Escolhida: Playfair Display.** Serifa de alto contraste, a mais próxima dos títulos do brand kit, com acentos corretos e itálico elegante para a frase-conceito.
- Texto: **Jost** (sans geométrica, eco dos posts do Instagram).
- Duas famílias via Google Fonts com `preconnect` e `display=swap`:
  - Playfair Display 400, 500 e itálico 400;
  - Jost 400, 500 e 600.

| Papel | Tamanho |
| --- | --- |
| h1 | `clamp(2.125rem, 1.4rem + 3.3vw, 4rem)` (≤ 64px), Playfair 400, entrelinha 1,08 |
| h2 | `clamp(1.875rem, 1.35rem + 2vw, 2.75rem)`, Playfair 400 |
| Subtítulo e frase-conceito | Playfair itálico, `clamp(1.25rem, 1.05rem + .8vw, 1.5rem)` |
| h3 (cartões e serviços) | Playfair 500, `1.3rem` |
| Corpo | Jost 400, `1.0625rem`, entrelinha 1,65 |
| Pequeno | Jost 400, `0.9375rem` |
| Rótulo e selo | Jost 500, `0.8125rem`, caixa alta, tracking 0,16em |

## Estilo

### Fundos (nunca vidro sobre cor lisa)

- **Hero:** foto da sala de atendimento nítida em tela cheia (`<img>`, `object-fit: cover`), parallax suave. Por cima:
  - degradê quente escuro (espresso/noz: mais forte em cima, no centro do texto e embaixo);
  - grão leve (SVG `feTurbulence` em data URI, que funciona por `file://`).
- **Seções claras:** creme/areia com **manchas de luz animadas** (orbes em degradê radial de ouro, ouro-claro e noz, com deriva lenta de 24-40s) atrás das placas de vidro.
- **Capítulo escuro contínuo** (Avaliações + Agende sua visita + rodapé num só fundo):
  - degradê longo creme → areia → noz → espresso no topo (sem corte);
  - manchas âmbar e noz animadas no espresso;
  - leve variação para noz no meio;
  - espresso no rodapé.

### Liquid glass (linguagem dominante)

**Material comum** a todos os vidros:

- `backdrop-filter: blur() saturate()`;
- tinta translúcida;
- **borda especular viva**: 1px em degradê forte no canto superior esquerdo, fraco no meio e claro no inferior direito, por pseudo-elemento com máscara;
- brilho interno no topo e sombra interna embaixo;
- **reflexo radial que segue o ponteiro** (`--mx`/`--my`);
- sombra externa suave.

**Refração** (aprimoramento progressivo):

- filtro SVG `feTurbulence` + `feDisplacementMap` no `backdrop-filter`, ativado por classe só no Chromium;
- **distorção mais forte ao hover**, com um segundo filtro de escala maior;
- Safari e Firefox recebem desfoque + borda especular + reflexo.

| Variante | Tinta | Onde |
| --- | --- | --- |
| Vidro escuro | espresso 0,38-0,55 | pílula de navegação, selos do hero, depoimentos, bloco de contato e horários, nota 5,0 |
| Vidro claro | creme 0,5-0,6 | cartões dos pilares, placas de serviço, legendas de foto, botão secundário no claro |
| Vidro claro sobre foto | branco 0,06-0,22 em degradê | botões secundários sobre foto e no escuro (texto claro) |
| Vidro champanhe | ouro-claro 0,16-0,30 em degradê, borda dourada interna | botão principal "Agendar" sobre foto e no escuro (texto claro, **nunca** amarelo chapado, **sem** halo colorido) |
| Vidro espresso | noz/espresso 0,72-0,82, borda dourada | botões "Agendar" sobre fundo claro (texto claro, AA) |
| Vidro verde | verde-wa 0,8 | WhatsApp flutuante |
| Vidro escuro denso | espresso 0,9 | menu móvel aberto (legibilidade) |

- **Fallback:** `@supports not (backdrop-filter)` e `@media (prefers-reduced-transparency: reduce)` trocam cada tinta pela mesma cor com opacidade 0,92-0,96, mantendo borda, brilho e sombra. AA garantido no fallback.
- **Limite:** a regra de 3 a 5 blocos de vidro por tela está **suspensa por pedido explícito do usuário**. Vidro só em elementos (chips, botões, placas, cartões curtos, legendas, bloco de contato), nunca em volta do h1 ou de parágrafos longos de seção.

### Divisores e números

- Filetes finos (1px, ouro a 40-60%) na linha do tempo de "Como funciona" e na citação de "Sobre".
- Numeração tipográfica discreta (Playfair, ouro-texto) nos passos e nas placas de serviço. Sem círculos.

### Movimento (Motion + Lenis, aprovado pelo usuário com nota 10)

Implementado em `src/components/Motion.tsx` e nos componentes de seção; manter esta intensidade.

- **Rolagem suave com inércia** (Lenis, `lerp` 0,085); âncoras descontam 96px do cabeçalho.
- **Hero:**
  - título palavra por palavra e selos/CTAs em sequência por CSS (rodam antes da hidratação, sem atrasar o LCP);
  - ao rolar para fora: foto aproxima (1 → 1,18) e desce; conteúdo sobe, encolhe (0,9), desfoca e some;
  - CTA principal **magnético** (segue o cursor e volta com mola).
- **Títulos de seção:** cada palavra sobe de dentro de uma máscara, em sequência (`SplitHeading`).
- **Entradas:** subida de 56px saindo do desfoque, com stagger (`rise`); pilares "se levantam" em 3D (`flipUp`).
- **Inclinação 3D** que segue o cursor em pilares, placas de serviço e depoimentos (`useTilt`); só com mouse.
- **Fotos** abrem de dentro para fora (máscara `clip-path`) com a imagem desacelerando o zoom (`PhotoReveal`).
- **Linha do tempo** se desenha em dourado conforme a rolagem.
- **Citação de "Sobre"** acende palavra por palavra conforme a rolagem (`ScrollText`).
- **Avaliações** em trilho arrastável com inércia, encaixe com mola, elástico nas pontas e profundidade coverflow ligada à rolagem (`ReviewRail`), com setas e contador.
- **Serviços** em cartões que giram com arrasto, clique ou teclado (`FlipCard`): frente com a descrição, verso escuro com "Agendar" e WhatsApp citando o serviço.
- **Ondas suaves** em canvas no fundo de "Como funciona" e "Sobre": três senoides em areia, ouro e creme que deslizam e respiram devagar (`Waves`).
- **Linhas fluindo** em canvas atrás dos serviços (sutis) e do capítulo escuro (douradas), que se afastam do cursor (`FlowLines`).
- **Borda que acende** perto do cursor nos pilares e nos depoimentos (`BorderGlow`), só com mouse.
- **Botões:** texto que desliza revelando uma variação no hover ("Agendar horário" → "Ver horários livres"); estouro de partículas douradas só em ações que não trocam de página (WhatsApp).
- **Menu do celular** em cartões ("Tratamentos", "A Revitalle", "Visite") que se expandem da pílula do cabeçalho.
- **Contadores** 5,0 e 11 sobem ao entrar na tela; manchas de luz em deriva lenta; header muda ao rolar.
- **Hover:** reflexo do vidro segue o ponteiro, refração mais forte, brilho deslizante e seta que avança nos botões.
- **`prefers-reduced-motion`:** tudo estático e visível, sem Lenis, sem coverflow, linhas desenhadas paradas, cartões giram sem animação, sem partículas, contadores no valor final.

## Tratamento das fotos

| Foto | Onde | Tamanho máximo | Tratamento |
| --- | --- | --- | --- |
| `fotos/espaço2.png` (876x871) | Fundo do hero, tela cheia | tela cheia (ampliada) | **Exceção registrada:** é o único jeito de ter o hero "como a referência". Origem sem desfoque além de 1px, leve aquecimento para neutralizar o azul do LED, degradê quente escuro e grão por cima para mascarar a ampliação |
| `fotos/espaço.png` (683x910) | Como funciona, foto principal da colagem | ~480x640 CSS px | Retângulo com cantos de 20px, abre por máscara, legenda em vidro "Recepção · Bairro Beatriz, Lages" (no topo no celular) |
| `fotos/espaço2.png` (876x871) | Como funciona, segunda foto sobreposta | ~50% da colagem | Quadrada, em moldura de vidro claro, com parallax próprio e legenda "Sala de atendimento" |
| `servicos/0.png` recortada em quadrado (458x458, sem os textos embutidos) | Sobre | ≤ 480px (ampliação leve) | **Provisório** (comentário PLACEHOLDER), cantos de 20px, moldura fina dourada deslocada atrás, legenda em vidro |
| `fotos/2.png`, `fotos/image.png` | - | - | Não usar (briefing) |

**Logo:**

- `<img>` real em WebP limpo, gerado do logo marrom oficial (sem as franjas dos PNG dourados):
  - horizontal dourado (cabeçalho);
  - vertical dourado (rodapé, ≤ 80px);
  - horizontal e vertical marrom (fundos claros, reserva);
  - versões PNG para a cliente.
- A versão horizontal recompõe as partes do logo na disposição do `Logo2` oficial.

## Estrutura de seções

Todas com altura ≤ ~900px em 1440x900. CTA "Agendar" → `/agendar/` no cabeçalho, no hero, ao fim de serviços, nas avaliações e no bloco final. WhatsApp secundário.

1. **Cabeçalho:** pílula de vidro flutuante com logo dourado, links (Serviços, Como funciona, Sobre, Avaliações, Visite) e "Agendar" em vidro dourado. No celular: logo, "Agendar" e menu em vidro.
2. **Hero** (100svh, 680-900px):
   - foto nítida da sala;
   - centralizados, nesta ordem: selo de vidro "O 1º Head Spa de Lages", h1 "Seu momento de se reconectar com você", subtítulo em itálico, CTAs ("Agendar horário" em vidro dourado e "Tirar dúvidas no WhatsApp" em vidro claro), selos "5,0 no Google · 11 avaliações" e "Atendimento individual".
3. **Pilares:** 3 cartões de vidro claro sobrepostos à base do hero.
   - Consulta individual;
   - Ambiente para desacelerar;
   - Tecnologia e saúde capilar.
4. **Serviços** (creme com manchas):
   - título centralizado e linha de apoio;
   - 7 placas de vidro em grade de 3 colunas ("Consulta" maior, com CTA);
   - nota de valores e aviso de saúde + botão "Ver horários e agendar".
5. **Como funciona** (areia com manchas): foto da recepção à esquerda; à direita, título e linha do tempo fina com 4 passos numerados tipograficamente.
6. **Sobre** (creme): texto da cliente, citação com filete, assinatura; foto provisória da Marineuza com legenda em vidro.
7. **Avaliações** (início do capítulo escuro, sem emenda):
   - cabeçalho centralizado: título, nota 5,0 em pílula de vidro com contador e estrelas ao lado do CTA grande;
   - trilho arrastável de 5 depoimentos em vidro escuro, todos em Jost (sem itálico), com estrelas, texto, inicial em círculo dourado, nome e "Avaliação no Google".
8. **Agende sua visita** (mesmo fundo escuro): título, frase da cliente e CTAs soltos; ao lado, bloco de vidro com endereço, horários e contato.
9. **Rodapé** (mesmo fundo): logo vertical dourado ≤ 80px, frase, navegação e legal.
10. **WhatsApp flutuante** em vidro verde: 56px, `aria-label`, safe-area; some quando o bloco final está visível.

## Referências

### Aproveitado

| Traço | Origem |
| --- | --- |
| Hero com foto do ambiente em tela cheia e texto centralizado direto sobre a foto | mock |
| Menu em pílula translúcida flutuando sobre a foto, botão de ação em pílula | mock |
| Três cartões de vidro com ícone de linha, título e texto, sobrepostos à base do hero | mock |
| Título de serviços centralizado, grade de itens com cantos arredondados | mock |
| Neutros quentes e serifa elegante nos títulos | mock e brand kit |
| Paleta oficial, logo, conceito "sofisticação, bem-estar, calor e exclusividade" | brand kit |
| Espresso com luz dourada nos capítulos escuros | brand kit (cartão do logo) |
| Filetes finos dourados, rótulos em caixa alta espaçada | brand kit |
| Liquid glass dominante (refração, borda especular, reflexo, profundidade) | estilo desejado + pedido do usuário |

### Deixado de fora

| Traço | Motivo |
| --- | --- |
| Foto por serviço na grade (mock) | Não há fotos por serviço; placas de vidro tipográficas |
| Mármore literal (brand kit) | Sem foto real de mármore; o espaço não tem mármore |
| Logos dourados em PNG originais | Franjas de recorte; usado o logo marrom limpo recolorido |
| Fundos de foto superdesfocados e moldura em arco (versão anterior) | Reprovados pelo usuário |
| Lente WebGL (versão anterior) | Ampliaria a pixelização da foto nítida; ver "Direção escolhida" |
| Paletas sugeridas pela `ui-ux-pro-max` | Conflitam com o brand kit |

## Fontes do conteúdo

- **Briefing:** nome, posicionamento, serviços, endereço, coordenadas, horários, WhatsApp, Instagram, nota 5,0 com 11 avaliações, depoimentos, proprietária, textos do Instagram.
- **Artes do Instagram** (`assets/servicos/1-7.png`): descrições dos serviços.
- **Site atual:** não há.
- **Textos novos** (para aprovação; lista no resumo final): títulos de seção, descrições de saúde reescritas, pilares, passos, microcopy e mensagens de WhatsApp.

## Acabamento

**Critique (`/impeccable critique`) da revisão 2.** Execução degradada em contexto único: sem subagentes, porque o ambiente só os permite a pedido explícito do usuário; sem snapshot em `.impeccable/critique/`, que fica fora de `web-cliente/`.

- **Detector `impeccable detect`:** 0 achados.
- **Nota:** **26/28** (heurísticas 7, 9 e 10 n/a em landing sem formulário).

| # | Heurística | Nota |
| --- | --- | --- |
| 1 | Visibilidade do estado (header muda ao rolar, foco visível, menu com `aria-expanded`) | 4 |
| 2 | Correspondência com o mundo real (foto real, linguagem da cliente) | 4 |
| 3 | Controle e liberdade (Esc fecha o menu, âncoras com rolagem suave) | 4 |
| 4 | Consistência (um sistema de vidro, um par tipográfico) | 4 |
| 5 | Prevenção de erros (CTAs nomeiam a ação; aviso de saúde) | 3 |
| 6 | Reconhecer em vez de lembrar (CTA "Agendar" sempre no cabeçalho) | 4 |
| 8 | Estética e minimalismo (vidro em elementos, sem card no h1) | 3 |

Achados e correções aplicadas:

- **`layout`:**
  - Serviços passou de 1089px para 894px (grade de 4 colunas).
  - Seções claras num só fundo contínuo, com manchas compartilhadas e máscara no topo: sem emendas.
  - Capítulo escuro contínuo (Avaliações + Visite + rodapé).
  - Fade da base do hero para o creme sob os cartões.
  - Hero em `calc(100svh - 70px)` para os pilares aparecerem na primeira dobra.
- **`colorize`:**
  - Manchas âmbar e ouro mais saturadas atrás das placas, para o vidro aparecer.
  - Tinta das placas 0,34.
  - `--ouro-texto` escurecido para `#634820`, mantendo AA sobre o vidro claro.
- **`polish`:**
  - Contraste medido no navegador sobre o fundo real (texto escondido, luminância do fundo no pior ponto). Hero desktop: h1 5,7:1, subtítulo 7,6:1, selos ≥ 8:1. Placas ≥ 7,5:1.
  - Botão secundário trocado para vidro claro com texto espresso (13,7:1), porque vidro claro com texto claro reprovava.
  - Degradê mais forte no hero do celular (subtítulo de 3,8:1 para 6,2:1).
  - Legenda das fotos com tinta 0,72.
  - Sublinhados dos links refeitos (sem traços soltos).
  - Menu móvel em vidro denso.
  - Tamanho mínimo de 13px nos rótulos.
- **`adapt`:** verificado em 1440, 768 e 375; h1 em 2 linhas no celular; WhatsApp flutuante some no bloco final.
- **`bolder`:** não rodou (o vidro e a foto já carregam a página).
