package org.yeshira.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.jcouchdb.document.BaseDocument;
import org.springframework.stereotype.Service;
import org.yeshira.model.Document;
import org.yeshira.model.Paragraph;
import org.yeshira.model.service.DocumentService;
import org.yeshira.model.validators.DocumentValidator;
import org.yeshira.web.controllers.validation.ValidationError;
import org.yeshira.web.controllers.validation.exceptions.ValidationException;

@Service
public class DocumentServiceImpl extends AbstractService implements DocumentService {

	@Override
	public void saveDocument(Document document, List<Paragraph> paragraphs) {
		// TODO: validate document
		DocumentValidator dv = new DocumentValidator(document);
		Collection<ValidationError> validate = dv.validate();
		if (!validate.isEmpty()) {
			throw new ValidationException(validate);
		}
		
		BaseDocument baseDocument = document.getBaseDocument();
		if (document.isSaved()) {
			for (Paragraph par : paragraphs) {
				saveParagraph(par, document);
			}
			document.setParagraphs(paragraphs);
			db.updateDocument(baseDocument);
		} else {
			// save document to get document id
			db.createDocument(baseDocument);
			// save paragraphs
			for (Paragraph par : paragraphs) {
				saveParagraph(par, document);
			}
			// modify document to point to paragraphs and save
			document.setParagraphs(paragraphs);
			db.updateDocument(baseDocument);
		}

	}

	/**
	 * Paragraph can only be saved as part of a document
	 * 
	 * @param par
	 * @param document
	 */
	private void saveParagraph(Paragraph par, Document document) {
		if (par.getDocumentId() != null
				&& !par.getDocumentId().equals(document.getId())) {
			throw new ValidationException(
					"Cannot re-assign a paragraph to a different document");
		}
		par.setDocument(document);

		// TODO: validate paragraph
		if (par.isSaved()) {
			db.updateDocument(par.getBaseDocument());
		} else {
			db.createDocument(par.getBaseDocument());
		}
	}

	@Override
	public Document getDocument(String documentId) {
		return (Document) getById(documentId);
	}

}
