# Ícone do app Questões

## Onde colocar (Android Studio)
Copie o conteúdo das pastas abaixo direto para `app/src/main/res/`, mesclando com o que já existe:

- `mipmap-mdpi/`, `mipmap-hdpi/`, `mipmap-xhdpi/`, `mipmap-xxhdpi/`, `mipmap-xxxhdpi/`
  → cada uma tem `ic_launcher.png`, `ic_launcher_round.png` (fallback pra Android < 8) e `ic_launcher_foreground.png` (camada do adaptive icon)
- `mipmap-anydpi-v26/ic_launcher.xml` e `ic_launcher_round.xml`
  → apontam pro adaptive icon (Android 8+): fundo = cor sólida, frente = o PNG de cada densidade
- `values/ic_launcher_background.xml`
  → declara a cor de fundo `ic_launcher_background` (#16324a, Tinta Prancheta). **Se você já tiver uma `colors.xml` com esse mesmo nome de recurso, não copie o arquivo — só adicione a linha `<color name="ic_launcher_background">#16324a</color>` na sua `colors.xml` existente**, senão dá conflito de recurso duplicado.

Depois de copiar, confirme no `AndroidManifest.xml` que a tag `<application>` já aponta:
```
android:icon="@mipmap/ic_launcher"
android:roundIcon="@mipmap/ic_launcher_round"
```
(o padrão do Android Studio já vem assim; só confirme que não foi alterado.)

## Outros arquivos
- `playstore-icon-512.png` — ícone 512×512 flat, padrão de ficha de loja (não necessário pro build do APK, é só referência/reserva).
- `ic_launcher_legacy.svg`, `ic_launcher_foreground.svg`, `playstore_icon.svg` — fontes vetoriais, caso queira regerar em outro tamanho ou ajustar depois.

## Paleta usada (Carta Anotada)
- Fundo: Tinta Prancheta `#16324a`
- Caixinha: Marco Dourado `#d79b3d`
- Check: Musgo `#7c9a5c`
