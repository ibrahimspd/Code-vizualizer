package endpoints;

import data.ParserDao;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import io.javalin.util.FileUtil;

import java.io.InputStream;

public class GraphController
{
    public static Handler fetchGraphData = ctx -> {
        UploadedFile uploadedFile = ctx.uploadedFiles().get(0);
        try(InputStream inputStream = uploadedFile.content())
        {
            FileUtil.streamToFile(inputStream, "/Users/ibra/filer/fil.zip");
            ParserDao.getInstance().parseFunctions();
        }catch (Exception e){
            e.printStackTrace();
        }
    ctx.result("File uploaded");
    };

}
