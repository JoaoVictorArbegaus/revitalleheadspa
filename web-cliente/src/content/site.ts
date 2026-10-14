// Conteúdo da landing. Fonte: design/briefing.md (textos novos aprovados conforme design/direcao-visual.md).

const WHATSAPP = '5549999657420';
const whatsapp = (mensagem: string) => `https://wa.me/${WHATSAPP}?text=${encodeURIComponent(mensagem)}`;

export const contato = {
  telefone: '(49) 99965-7420',
  telefoneHref: 'tel:+5549999657420',
  instagram: '@revitalleheadspa',
  instagramHref: 'https://www.instagram.com/revitalleheadspa/',
  endereco: 'R. Euclídes Cardoso, 141',
  bairroCidade: 'Beatriz, Lages - SC · CEP 88505-093',
  comoChegarHref: 'https://www.google.com/maps/search/?api=1&query=-27.81183604307785%2C-50.335274558146594',
  whatsappDuvida: whatsapp('Olá! Vi o site da Revitalle e gostaria de tirar uma dúvida sobre os tratamentos.'),
  whatsappAgendar: whatsapp('Olá! Vi o site da Revitalle e quero agendar um horário.'),
  whatsappInfo: whatsapp('Olá! Vim pelo site da Revitalle Head Spa e gostaria de mais informações.'),
};

export const AGENDAR_HREF = '/agendar/';

export const navegacao = [
  { href: '#servicos', rotulo: 'Serviços' },
  { href: '#como-funciona', rotulo: 'Como funciona' },
  { href: '#sobre', rotulo: 'Sobre' },
  { href: '#avaliacoes', rotulo: 'Avaliações' },
  { href: '#visite', rotulo: 'Visite' },
];

export const avaliacaoGoogle = { nota: 5, total: 11 };

export const pilares = [
  {
    icone: 'consulta',
    titulo: 'Consulta individual',
    texto: 'Análise do couro cabeludo e dos fios para indicar o tratamento ideal para você.',
  },
  {
    icone: 'ambiente',
    titulo: 'Ambiente para desacelerar',
    texto: 'Aromas, música e cuidado em cada detalhe, em um espaço pensado para relaxar.',
  },
  {
    icone: 'tecnologia',
    titulo: 'Tecnologia e saúde capilar',
    texto: 'Protocolos com laser, LED e vapor de ozônio unidos ao relaxamento.',
  },
] as const;

export const servicos = [
  {
    nome: 'Hair Spa',
    descricao:
      'Experiência completa que une limpeza de pele, spa dos pés e massagem nos pés, massagem craniana, no pescoço e nos ombros, hidratação profunda dos fios, vapor de ozônio e escova de cabelo.',
  },
  {
    nome: 'Anti-Queda Capilar',
    descricao:
      'Protocolo voltado a quem convive com queda capilar, alopecias ou disfunções do couro cabeludo. Sessões semanais que intercalam laser e LED de alta frequência, com relaxamento, esfoliação, blend de óleos, hidratação e escova.',
  },
  {
    nome: 'Fototerapia com LED',
    descricao:
      'Sessões de luz de LED sobre a pele e o couro cabeludo, associadas à hidratação profunda dos cabelos, como parte do cuidado com a saúde do couro cabeludo.',
  },
  {
    nome: 'Laser para Dores Crônicas',
    descricao:
      'Sessões de laser aplicadas nas regiões de dor crônica ou localizada, como um cuidado complementar voltado ao conforto no dia a dia.',
  },
  {
    nome: 'ILIB',
    descricao:
      'Sigla de Irradiação Intravascular do Sangue com Laser: terapia com laser aplicada na região do pulso, como um cuidado voltado ao bem-estar geral.',
  },
  {
    nome: 'Tratamento Integrado Capilar',
    descricao:
      'Combina massagem craniana, no pescoço e nos ombros, laser para dores, lavagem do cabelo e escova, em um cuidado completo para o cabelo.',
  },
];

export const etapas = [
  { titulo: 'Consulta', texto: 'Uma conversa e a análise do couro cabeludo e dos fios, sem pressa.' },
  { titulo: 'O protocolo certo', texto: 'Com base na avaliação, a Marineuza indica o tratamento e explica cada etapa.' },
  { titulo: 'O ritual', texto: 'Massagem, aromas, música e os cuidados do protocolo, em um ambiente pensado para desacelerar.' },
  { titulo: 'Finalização', texto: 'Escova e finalização. O horário é só seu: o atendimento é individual.' },
];

// PLACEHOLDER: depoimentos públicos do Google; confirmar com a proprietária a autorização de uso.
export const depoimentos = [
  {
    nome: 'Tamara F.',
    texto:
      'Minha experiência foi maravilhosa! Nunca tinha ido em um Head Spa e realmente me surpreendeu desde o acolhimento, até a explicação sobre o meu cabelo e principalmente o tratamento que foi suuuper relaxante. Já virei cliente!',
  },
  {
    nome: 'Aline S.',
    texto:
      'Minha experiência no SPA de terapia capilar foi excelente. Desde a recepção, percebi o cuidado com os detalhes, o espaço é aconchegante, perfumado e acolhedor. Fui muito bem atendida, me explicou cada etapa do tratamento com carinho, paciência e profissionalismo.',
  },
  {
    nome: 'Fabricio R.',
    texto: 'Experiência incrível! O ambiente é super acolhedor, cheiroso e relaxante. Saí renovado e com o cabelo maravilhoso!',
  },
  {
    nome: 'Salete A.',
    texto:
      'Um ambiente super aconchegante e muito agradável. Amei o atendimento. Os produtos maravilhosos e super indico. Com certeza voltarei sempre.',
  },
  { nome: 'Camilian S.', texto: 'Atendimento perfeito! Lugar aconchegante com pessoas maravilhosas. Super recomendo!' },
];

export const horarios = [
  { dias: 'Segunda a quinta', horario: '10h às 21h' },
  { dias: 'Sexta', horario: '10h às 17h' },
  { dias: 'Sábado e domingo', horario: 'Fechado' },
  // PLACEHOLDER: horário em feriados
  { dias: 'Feriados', horario: '[PLACEHOLDER: a confirmar]' },
];
