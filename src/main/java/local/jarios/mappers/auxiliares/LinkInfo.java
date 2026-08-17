package local.jarios.mappers.auxiliares;

import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.FeedType;

/** Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio */
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public final class LinkInfo {

  private String linkFirst;
  private String linkPrev;
  private String linkSelf;
  private String linkNext;

  public static LinkInfo getLinkInfoFromFeedType(FeedType feedType) {

    //
    LinkInfo linkInfo = new LinkInfo();

    feedType.getLink().stream()
        .filter(linkType -> linkType.getRel() != null)
        .forEach(
            linkType -> {
              switch (linkType.getRel()) {
                case Constantes.LINK_FIRST -> linkInfo.setLinkFirst(linkType.getHref());
                case Constantes.LINK_PREV -> linkInfo.setLinkPrev(linkType.getHref());
                case Constantes.LINK_SELF -> linkInfo.setLinkSelf(linkType.getHref());
                case Constantes.LINK_NEXT -> linkInfo.setLinkNext(linkType.getHref());
                default ->
                    log.error(
                        Mensajes.MENSAJE_VALOR_SWITCH_INCORRECTO,
                        "linkType.getRel()",
                        linkType.getRel());
              }
            });

    return linkInfo;
  }
}
