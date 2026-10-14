# Opções de direção visual - Revitalle Head Spa

> Etapa 1 do `gerador-landing`, revisada após a definição do **Estilo desejado: glassmorphism no estilo "liquid glass"** (briefing, seção 9).
> As 3 opções são variações de liquid glass sobre a identidade do brand kit. Nada foi construído ainda.
> Escolhida pelo usuário: Opção C (Lente Âmbar).

## Base comum (vale para as 3 opções)

### Entrada analisada (Etapa 0)

- **Briefing:** lido por completo. **Estilo desejado:** liquid glass (vidro translúcido com refração, brilho de borda e profundidade) sobre dourado, espresso e areia. "O que o cliente NÃO quer", tom de voz, público, tratamento e mensagem padrão do WhatsApp estão em branco (a página usará "você", tom acolhedor e sofisticado, a confirmar).
- **Referências** (sem `notas.md`, leitura feita pelas imagens):
  - `referencias/RevitalleHeadSpa_BrandKit.png` (1055x1491), página 1 de 4. Traços: fundo creme papel, títulos em serifa de alto contraste, rótulos em serifa caixa alta com tracking largo, filetes finos dourados, marcadores numerados em círculo dourado (01-05), frase-conceito em serifa itálica, cartão do logo dourado em relevo sobre mármore espresso com veios dourados e ripado de madeira. Paleta oficial: Ouro Principal `#C8A062`, Ouro Claro `#E2C693`, Espresso `#23140F`, Marrom Noz `#4A2C1B`, Areia Suave `#EDE3D4`.
  - `referencias/6a0ea2a682728ccffd2bcba5f9fb678c.png` (736x1318), mock de salão. Traços: neutros quentes, títulos em serifa fina, menu em pílula translúcida sobre a foto, botões em pílula, faixa de foto desfocada com 3 cartões de vidro sobrepostos, grade de serviços com foto de cantos arredondados.
  - **Traços recorrentes:** serifa elegante, neutros quentes, muito respiro, acento metálico/dourado contido, composição centrada. O mock já é, em essência, glassmorphism (pílula e cartões de vidro sobre foto desfocada), o que confirma o estilo desejado.
  - **Traços isolados:** mármore escuro com veios (só brand kit), marcadores numerados em círculo (só brand kit), hero em foto de tela cheia (só mock; inviável com as fotos atuais, ver abaixo).
- **Site atual:** não há URL no briefing; conteúdo vem do briefing, das artes `assets/servicos/1-7.png` e dos textos do Instagram transcritos.

### Inventário de fotos (resolução real)

| Arquivo | Pixels | O que mostra | Uso possível |
| --- | --- | --- | --- |
| `fotos/espaço.png` | 683x910 (retrato) | Recepção real: ripado de madeira com logo dourado, parede verde, mesa branca com pés dourados, poltrona creme, luz do dia | Foto nítida no máximo ~480x640 CSS px (nunca acima de 683x910). Como fundo, só desfocada/escurecida |
| `fotos/espaço2.png` | 876x871 (quadrada) | Sala de atendimento real: maca com lavatório de luz azul, arco dourado, prateleiras com LED quente, velas | Nítida no máximo ~560x560. É o melhor "fundo de vidro": desfocada vira manchas âmbar quentes. O azul do lavatório pede degradê/filtro quente por cima |
| `servicos/0.png` | 658x838 | Foto real da proprietária com texto embutido ("@REVITALLEHEADSPA", "MARINEUZA BRANCO", "NOSSOS SERVIÇOS") | Fora do topo. Só recorte provisório (faixa central ~658x440, sem os textos) na seção "Sobre", marcado para troca pelo original sem texto |
| `fotos/2.png`, `fotos/image.png` | 907x904, 724x866 | Retratos provavelmente editados por IA | **Não usar** (briefing: evitar) |
| `servicos/1.png`-`7.png` | ~600-690 px | Artes de texto do Instagram (creme, sans geométrica pesada) | Só fonte de conteúdo |
| Logos `RevitalleHeadSpa_Logo*.png` | 1254-1672 px, PNG transparente | Marrom chapado (limpo) e dourado metálico | O marrom é limpo. Os dourados (`Logo2`, `Logo4`) têm **franjas/pontilhado amarelo e vermelho nas bordas** (sobra de recorte), visíveis sobre fundo liso e sobre vidro escuro |

**Conclusão:** nenhuma foto sustenta um hero de tela cheia nítido em 1440 px. Isso favorece o liquid glass: o fundo de tela cheia passa a ser a foto **desfocada e aquecida** (que não denuncia a baixa resolução), e a foto nítida entra em moldura de vidro no tamanho natural ou abaixo dele.

### Restrições de cor verificadas (WCAG)

| Par | Contraste | Uso |
| --- | --- | --- |
| Espresso `#23140F` sobre Areia `#EDE3D4` | 14,0:1 | texto principal no claro |
| Marrom Noz `#4A2C1B` sobre Areia | 9,9:1 | texto secundário no claro |
| **Ouro `#C8A062` sobre Areia** | **1,9:1 (reprova)** | ouro nunca é texto em fundo claro nem em vidro claro: só filete, brilho de borda, ícone decorativo ou fundo de botão |
| Ouro escuro derivado `#7A5C2C` sobre creme `#F7F2EA` | 5,6:1 | rótulos "dourados" legíveis no claro |
| Espresso sobre Ouro `#C8A062` | 7,4:1 | botão principal "Agendar" |
| Ouro Claro `#E2C693` sobre Espresso | 10,8:1 | rótulos e destaques no escuro |
| Areia sobre Espresso | 14,0:1 | texto no escuro |
| Ouro sobre Marrom Noz | 5,2:1 | rótulo no marrom |

Sobre vidro, o contraste é medido contra o **pior ponto do fundo composto** (vidro + o que está atrás). Por isso o vidro que leva texto tem tinta mínima garantida (ver cada opção), e o vidro de opacidade baixa fica só para pílulas, filetes e molduras.

### Liquid glass: como fica em HTML/CSS puro (vale para todas)

- **Material:** `backdrop-filter: blur(14-24px) saturate(140-180%)` + tinta translúcida (creme ou espresso) + **brilho de borda especular** (borda de 1px em degradê claro→transparente via máscara, mais `box-shadow` interno claro no topo e escuro na base) + sombra externa suave para profundidade + leve "reflexo" diagonal em degradê.
- **Refração:** filtro SVG `feTurbulence` + `feDisplacementMap` aplicado ao fundo do vidro só onde o navegador suporta (`backdrop-filter: url(#...)` funciona hoje em Chromium). Em Safari e Firefox o vidro aparece sem a distorção, só com desfoque e brilho de borda. É aprimoramento progressivo, nunca requisito.
- **Camadas:** fundo (foto desfocada/aquecida ou campos de cor em degradê) → vidro → conteúdo. O conteúdo nunca fica "dentro" de vidro de baixa opacidade.
- **Movimento:** brilho de borda que acompanha levemente o ponteiro nos cartões principais e transição "líquida" da pílula de navegação; tudo desligado em `prefers-reduced-motion`.
- **Fallbacks:** `@supports not (backdrop-filter: blur(1px))` troca o vidro por superfície sólida na mesma tinta com opacidade alta (0,92-0,96), mantendo borda e sombra; `@media (prefers-reduced-transparency: reduce)` faz o mesmo. Contraste AA garantido no fallback.
- **Limite:** no máximo 3 a 5 blocos de vidro por tela (1440x900 e 375x812).

### Saída da `ui-ux-pro-max` e o que foi aproveitado

- `--design-system "head spa luxury liquid glass warm gold"`: estilo **Liquid Glass** (lensing, refração, translucidez adaptativa; "aplicar com parcimônia em navegação e controles, conteúdo em camada separada, testar reduced transparency e reduced motion, verificar contraste"), padrão de página com CTA repetido no hero, após serviços e no fim.
- Consulta anterior (`"head spa wellness luxury warm gold"`): padrão Hero + Depoimentos + CTA, sombras suaves, foco visível.
- **Aproveitado:** vidro concentrado em navegação, CTA e cartões principais; conteúdo longo fora do vidro; teste de transparência reduzida; prova social antes do CTA final; CTA repetido.
- **Descartado:** paletas sugeridas (rosa/lavanda e neutro frio `#1C1917`), porque o brand kit manda; a regra "evitar dark mode" (o brand kit tem a variante espresso); carrossel automático de depoimentos (5 depoimentos curtos cabem em grade).
- Pares tipográficos consultados: Playfair Display + Inter, Cormorant + Montserrat, Bodoni Moda + Jost.

### Estrutura de seções (comum; detalhada no `direcao-visual.md` após a escolha)

1. Cabeçalho: logo, navegação curta, botão "Agendar" (`/agendar/`).
2. Hero: "O 1º Head Spa de Lages", h1 (≤ 64px), frase da cliente, CTA "Agendar" + link secundário de WhatsApp.
3. Serviços: os 7 serviços sem preço/duração, "Consulta" destacada como primeiro passo, CTA "Agendar" ao fim.
4. A experiência / como funciona: consulta, avaliação do couro cabeludo, ritual (texto da cliente).
5. Sobre a Marineuza: trecho da história do Instagram.
6. Depoimentos: 5 avaliações do Google + "5,0 no Google, 11 avaliações".
7. O espaço: as duas fotos reais.
8. Endereço, horários e contato (mapa por coordenadas; link do Maps pendente).
9. CTA final + rodapé (logo ≤ 80px de altura).

Em todas as opções, Anti-Queda, Laser para Dores Crônicas e ILIB serão descritos pelo que é feito na sessão, sem promessa de resultado, com aviso para confirmar a redação e a habilitação profissional antes de publicar.

---

## Opção A - Vidro de Âmbar (liquid glass fiel sobre a base clara e quente)

**Descrição:** o liquid glass sobre a recepção da Revitalle: creme, areia, madeira e dourado, como vidro claro iluminado pelo sol. É a leitura mais fiel do brand kit e do espaço real, com o vidro do mock levado ao nível "liquid".

#### Traços de estilo

- **Superfícies:** base Areia `#EDE3D4` / creme `#F7F2EA`. Para o vidro ter o que refratar, o fundo das telas com vidro recebe **campos de cor quentes** (degradês radiais em Ouro Claro, areia e um toque de madeira/noz, desfocados) e, no hero, `espaço.png` desfocada e clareada. Rodapé em Espresso com logo marrom invertido para areia ou dourado limpo.
- **Vidro:** tinta creme `rgba(247,242,234,.55-.72)`, `blur(20px) saturate(160%)`, borda especular dourada clara (degradê `#FFFFFF`→`#E2C693`→transparente), sombra externa espresso a 10-14%.
- **Tipografia:** Cormorant Garamond (títulos 500/600, itálico na frase-conceito, rótulos em caixa alta com tracking largo) + Jost (texto 400/500). h1 `clamp(2.5rem, 4.5vw, 4rem)`.
- **Paleta:** texto Espresso; Ouro `#C8A062` só em brilho de borda, filetes, ícones e fundo do botão principal; rótulos em ouro escuro `#7A5C2C`.
- **Composição:** hero dividido (painel de vidro com h1 e CTAs à esquerda, foto nítida da recepção em moldura de vidro em arco à direita), marcadores numerados em círculo de vidro dourado para "como funciona".

#### Onde o liquid glass fica evidente (por tela, máximo 5)

- Hero: pílula de navegação + painel do h1 + moldura em arco da foto + botão "Agendar" em pílula de vidro dourado (4).
- Faixa de destaques (como no mock): `espaço2.png` desfocada e aquecida ao fundo, 3 cartões de vidro (Consulta, Hair Spa, Anti-Queda) + pílula de navegação (4).
- Serviços: cartões em superfície sólida creme com filete dourado; só o cartão "Consulta" é vidro, com a pílula (2).
- Depoimentos: painel de vidro com a nota 5,0 + pílula (2); os depoimentos em cartões sólidos.

**Fallback sem `backdrop-filter`:** vidro vira creme sólido `rgba(247,242,234,.95)` com a mesma borda dourada e sombra; texto Espresso a 14:1. Sem refração, o brilho de borda e o reflexo diagonal continuam marcando o material.

#### Como as fotos seriam usadas

- `espaço.png`: nítida na moldura em arco do hero (até ~460x610 px) e desfocada/clareada como fundo do hero.
- `espaço2.png`: desfocada e aquecida no fundo da faixa de destaques; nítida (até ~520x520) em "O espaço".
- `servicos/0.png`: recorte provisório em "Sobre" (~420 px de largura), moldura de vidro, marcado para troca.

**Principal risco:** vidro claro sobre fundo claro quase não aparece (pouco contraste entre o que está atrás e o vidro), e o dourado reprova como texto no claro (1,9:1). Mitigação: campos de cor quentes e foto desfocada atrás de todo vidro, borda especular marcada e sombra; nunca vidro sobre creme liso.

---

## Opção B - Obsidiana Dourada (liquid glass sobre espresso e mármore)

**Descrição:** o liquid glass sobre o cartão do logo do brand kit: espresso profundo com veios de mármore e luzes âmbar desfocadas, vidro escuro com borda dourada especular. É onde o vidro rende mais: brilho, refração e profundidade ficam nítidos.

#### Traços de estilo

- **Superfícies:** base Espresso `#23140F` com mármore sugerido em CSS/SVG (veios em Ouro a 6-10% de opacidade, sem imagem pesada) e manchas âmbar desfocadas vindas de `espaço2.png`. Cartões de conteúdo longo em Marrom Noz `#4A2C1B` sólido. Uma seção de respiro em Areia (depoimentos) para não cansar.
- **Vidro:** tinta espresso `rgba(35,20,15,.45-.60)`, `blur(18px) saturate(170%)`, borda especular em Ouro Claro (degradê `#E2C693`→`#C8A062`→transparente), reflexo diagonal claro a 8%, leve glow dourado atrás do CTA.
- **Tipografia:** Playfair Display (títulos 500/600, alto contraste como no brand kit) + Jost (texto em Areia, 14:1). Rótulos em Ouro Claro caixa alta.
- **Paleta:** Espresso e Marrom Noz como base; Ouro em degradê metálico discreto no botão principal, filetes e números; Ouro Claro para destaques de texto.
- **Composição:** hero centrado sobre `espaço2.png` desfocada e escurecida; painel de vidro com h1 e CTA; serviços em lista editorial com filetes; ripado de madeira em CSS como borda lateral de seções (eco do painel da recepção).

#### Onde o liquid glass fica evidente (por tela, máximo 5)

- Hero: pílula de navegação + painel central do h1 + botão "Agendar" em vidro dourado + selo de vidro "O 1º Head Spa de Lages" (4).
- Serviços: 3 cartões de vidro em destaque (Consulta, Hair Spa, Anti-Queda) + pílula; os outros 4 em lista sólida (4).
- Experiência: moldura de vidro da foto + painel de passos + pílula (3).
- CTA final: painel de vidro grande com logo, endereço e horários + pílula (2).

**Fallback sem `backdrop-filter`:** vidro vira Marrom Noz/Espresso sólido `rgba(35,20,15,.94)` com a mesma borda dourada; texto Areia a 14:1 e Ouro Claro a 10,8:1.

#### Como as fotos seriam usadas

- `espaço2.png` (luz quente + LED) conversa com o escuro: fundo desfocado do hero com filtro âmbar por cima (neutraliza o azul do lavatório) e nítida até ~520x520 em moldura de vidro.
- `espaço.png` (luz do dia, parede verde) destoa do escuro: moldura menor (~360x480) com degradê espresso na base e legenda "Recepção".
- `servicos/0.png`: recorte provisório em "Sobre", moldura de vidro escuro.

**Principal risco:** distância do espaço real, que é claro, de dia, branco e madeira; quem chega pode estranhar. As **franjas amarelas e vermelhas dos logos dourados** aparecem justamente sobre espresso e vidro escuro, então exigiria limpar o PNG (ou usar o logo marrom convertido para Areia). Mármore em CSS pode parecer falso se exagerado.

---

## Opção C - Lente Âmbar (claro e quente, com capítulos escuros onde o vidro brilha)

**Descrição:** a página clara e quente como o espaço real, mas aberta e fechada por "capítulos" em espresso onde o liquid glass aparece com força total (hero, depoimentos, CTA final). Entre eles, seções de leitura em creme com vidro claro discreto.

#### Traços de estilo

- **Superfícies:** hero, depoimentos e CTA final em Espresso com `espaço2.png` desfocada e aquecida ao fundo; serviços, experiência, sobre e endereço em creme/Areia com campos de cor quentes desfocados atrás do vidro claro. Transição entre claro e escuro por degradê longo, sem corte seco.
- **Vidro:** dois materiais da mesma família. Escuro (tinta espresso `.50`, borda Ouro Claro) nos capítulos; claro (tinta creme `.65`, borda branca→dourada) nas seções de leitura. Mesmo raio, mesma borda especular, mesma sombra: lê como um só sistema.
- **Tipografia:** Cormorant Garamond (títulos e rótulos em caixa alta, como no brand kit) + Jost (texto). Nomes de serviço em Jost 600 caixa alta com tracking, eco discreto das artes do Instagram.
- **Paleta:** Espresso/Areia como par principal; Ouro em bordas especulares, filetes, números e botão; ouro escuro `#7A5C2C` para rótulos no claro, Ouro Claro no escuro.
- **Composição:** hero em duas camadas (painel de vidro escuro com h1 e CTAs + foto nítida da recepção em moldura de vidro em arco); serviços em grade de cartões claros; "o ritual em 4 passos" com números grandes em serifa dourada dentro de círculos de vidro.

#### Onde o liquid glass fica evidente (por tela, máximo 5)

- Hero (escuro): pílula de navegação + painel do h1 + moldura em arco da foto + botão "Agendar" em vidro dourado (4).
- Serviços (claro): cartão "Consulta" em vidro claro + pílula; demais cartões sólidos creme com filete dourado (2).
- Experiência (claro): 4 círculos de passo em vidro + pílula (5, círculos pequenos).
- Depoimentos (escuro): painel de vidro com a nota 5,0 + 2 cartões de citação em vidro + pílula (4).
- CTA final (escuro): painel de vidro com CTA, endereço e horários + pílula (2).

**Fallback sem `backdrop-filter`:** vidro escuro vira Espresso sólido `.94` (Areia 14:1, Ouro Claro 10,8:1); vidro claro vira creme sólido `.95` (Espresso 14:1). Bordas, sombras e reflexo diagonal permanecem.

#### Como as fotos seriam usadas

- `espaço2.png`: desfocada e aquecida no fundo dos capítulos escuros; nítida (até ~520x520) em "O espaço".
- `espaço.png`: nítida na moldura em arco do hero (até ~460x610 px), legenda em pílula de vidro "Recepção, bairro Beatriz".
- `servicos/0.png`: recorte provisório em "Sobre", moldura de vidro claro, marcado para troca.

**Principal risco:** alternar claro e escuro pode fragmentar a página se as transições forem bruscas, e dois materiais de vidro exigem disciplina para parecerem um só. O logo no hero escuro precisa da versão dourada limpa ou do marrom convertido para Areia (franjas dos PNGs dourados).

---

## Recomendação

**Opção C - Lente Âmbar.** O liquid glass só fica evidente quando há contraste e luz atrás dele, o que acontece no escuro. Por outro lado, o espaço real e o brand kit são claros e quentes. A Opção C coloca o vidro com força total onde ele converte e impressiona (hero, depoimentos, CTA final, todos sobre a luz âmbar desfocada da sala de atendimento) e mantém a leitura dos serviços e da história em creme, fiel à recepção. O contraste AA é mais fácil de garantir no vidro escuro (Areia 14:1), e as fotos de baixa resolução viram fundo desfocado sem denunciar a qualidade. Se a preferência for a fidelidade total ao claro, a Opção A é a alternativa segura, com o risco de o vidro aparecer menos.

**Escolha do usuário:** Opção C - Lente Âmbar, com o acréscimo aprovado de um efeito WebGL de "lente de vidro líquido" somente no hero. Contrato em `direcao-visual.md`.
