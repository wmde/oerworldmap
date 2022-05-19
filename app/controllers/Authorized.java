package controllers;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 * @author fo
 */
class Authorized extends Action.Simple {

  private static final String USERNAME_HEADER = "X-username";
  private static final String USERID_HEADER = "X-userid";

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {
    if (ctx.request().hasHeader(USERNAME_HEADER)
      && ctx.request().hasHeader(USERID_HEADER)
    ) {
      ctx.request().setUsername(ctx.request().getHeader(USERNAME_HEADER));
    }
    return delegate.call(ctx);
  }

}
