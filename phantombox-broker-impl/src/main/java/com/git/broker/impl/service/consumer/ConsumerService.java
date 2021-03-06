package com.git.broker.impl.service.consumer;

import static com.git.broker.api.domain.Constants.CALL_RESPONSE_QUEUE;
import com.git.broker.api.domain.IRequest;
import com.git.broker.api.domain.IRequestCallback;
import com.git.broker.api.domain.IResponse;
import com.git.broker.api.domain.IResponseCallback;
import com.git.broker.api.service.consumer.IConsumerService;
import com.git.broker.api.service.factory.IMessageCreatorFactory;
import com.git.broker.api.service.marshaller.IMarshallerService;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.annotation.PostConstruct;
import javax.jms.Message;

/**
 * {@link IConsumerService} interface implementation.
 * <p/>
 * Date: 28.11.12
 * Time: 11:05
 *
 * @author rpleshkov
 */
public class ConsumerService implements IConsumerService {

    private static final Logger LOGGER = Logger.getLogger(ConsumerService.class);

    @Autowired
    @Qualifier("jettisonMarshallerService")
    private IMarshallerService marshallerService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private IMessageCreatorFactory messageCreatorFactory;

    @Autowired
    private IRequestCallback requestCallback;

    private IResponseCallback responseCallback;


    @PostConstruct
    private void init() {
        responseCallback = new IResponseCallback() {
            @Override
            public void response(IResponse response) {
                String msg = marshallerService.marshall(response);
                MessageCreator messageCreator = messageCreatorFactory.create(response);
                jmsTemplate.send(CALL_RESPONSE_QUEUE, messageCreator);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(Message message) {
        BasicConfigurator.configure();
        IRequest request = messageCreatorFactory.getRequest(message);
        requestCallback.request(request, responseCallback);
    }
}
