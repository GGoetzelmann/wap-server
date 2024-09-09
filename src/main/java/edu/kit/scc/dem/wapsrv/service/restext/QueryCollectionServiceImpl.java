package edu.kit.scc.dem.wapsrv.service.restext;

import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidRequestException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.ModelFactory;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.repository.AnnotationRepository;
import edu.kit.scc.dem.wapsrv.service.AnnotationService;
import org.apache.commons.rdf.api.Dataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QueryCollectionServiceImpl implements QueryCollectionService {

    @Autowired
    private AnnotationRepository repository;

    @Autowired
    protected ModelFactory modelFactory;


    @Override
    public Page getPage(Map<String, String> propertyMap) throws InvalidRequestException {
        Dataset retDs = repository.getRdf().createDataset();

        Page[] page = new Page[1];
        repository.readRdfTransaction(ds -> {
            List<Annotation> annoIRIs = repository.getAnnotationsByWADMPropertyValues(propertyMap);

            List<Annotation> undeletedAnnos = annoIRIs.stream().filter(anno -> !anno.isDeleted()).collect(Collectors.toList());
            if (undeletedAnnos.isEmpty()) {
                throw new InvalidRequestException(ErrorMessageRegistry.PAGE_WITH_PAGE_BUT_IRIS_MISSING);
            }

             page[0] = modelFactory.createPage(retDs, "dynamicCollection", 0, false, true, undeletedAnnos.size(), Instant.now().toString(), "dynamic collection for query");
            undeletedAnnos.forEach((anno -> page[0].addAnnotation(anno)));
            page[0].closeAdding();

        });
        return page[0];
    }
}
