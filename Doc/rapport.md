# DMA - Labo 2

Loris Marzullo - Thibault Seem - Joel Matias

## 1.1. Questions d’approfondissement

### 1.1.1 Est-ce que toutes les balises à proximité sont présentes dans toutes les annonces de la librairie ? Que faut-il mettre en place pour permettre de « lisser »  les annonces et ne pas perdre momentanément certaines balises ?

```
Non, elles ne sont pas toujours toutes affichées. On peut présumer que la librairie garde en mémoire les signaux reçus pendant un temps T, et tout les temps T il nous donne la liste desdits signaux. Ce qui peut provoquer la disparition d'une des balises de la liste qui n'a pas envoyé de signal (ou que le signal n'a pas été reçu) durant cette période fixe.

Afin de régler ce problème, au lieu de remplacer la liste existante à chaque nouvel appel du callback, nous pouvons mettre à jour la liste avec les nouveaux signaux et mettre à jour les informations de ceux existants si ceux-ci sont différents.


```



### 1.1.2 Nous souhaitons effectuer un positionnement en arrière-plan, à quel moment faut-il démarrer et éteindre le monitoring des balises ? Sans le mettre en place, que faudrait-il faire pour pouvoir continuer le monitoring alors que l’activité n’est plus active ?

```
Lors de l'appel à la fonction OnCreate(), nous pouvons démarrer le monitoring des balises et l'éteindre uniquement lors de l'appel au OnDestroy(). Et afin de le garder en place en arrière-plan, il faut déléguer l'appel aux fonctions internes du smartphone. Cela va fonctionner de la même manière que les notifications étant lancées même lorsque l'application est éteinte.

Pour le faire fonctionner, il faut mettre en place un BroadCastReceiver pour la communication avec les fonctions internes du téléphone.
```



### 1.1.3 On souhaite trier la liste des balises détectées de la plus proche à la plus éloignée, quelles sont les valeurs présentes dans les annonces reçues qui nous permettraient de le faire ? Comment sont-elles calculées et quelle est leur fiabilité ?

```
Une des valeurs envoyées dans les annonces est la distance. La distance est une estimation basée sur la force du signal bluethoot reçue du beacon au smartphone. La fiabilité est très mauvaise. Jusqu'à deux mètres, les données sont fiables de plus ou moins un mètre tandis qu'à vingt mètres, sont fiables à plus ou moins 10 à 40 mètres.
```

